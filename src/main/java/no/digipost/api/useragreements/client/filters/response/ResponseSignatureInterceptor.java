/**
 * Copyright (C) Posten Norge AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.digipost.api.useragreements.client.filters.response;

import no.digipost.api.useragreements.client.ServerSignatureException;
import no.digipost.api.useragreements.client.security.ClientResponseToVerify;
import no.digipost.api.useragreements.client.security.ResponseMessageSignatureUtil;
import no.digipost.api.useragreements.client.util.Supplier;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.RequestLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.HttpContext;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Optional;

import static no.digipost.api.useragreements.client.Headers.X_Digipost_Signature;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class ResponseSignatureInterceptor implements HttpResponseInterceptor {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final Supplier<byte[]> certificateSupplier;
	private final Base64.Encoder base64Encoder = Base64.getEncoder();
	private final Base64.Decoder base64Decoder = Base64.getDecoder();

	public ResponseSignatureInterceptor(final Supplier<byte[]> certificateSupplier) {
		this.certificateSupplier = certificateSupplier;
	}

	@Override
	public void process(HttpResponse response, HttpContext context) {
		HttpRequest request = (HttpRequest) context.getAttribute("http.request");
		if (skipResponseSignatureVerification(request)) {
			return;
		}

		Optional<byte[]> mayBeServerSignature = findServerSignatureInResponse(response);
		if (mayBeServerSignature.isPresent()) {
			byte[] serverSignature = mayBeServerSignature.get();
			try {
				String signatureString = ResponseMessageSignatureUtil.getCanonicalResponseRepresentation(new ClientResponseToVerify(context, response));

				Signature instance = Signature.getInstance("SHA256WithRSAEncryption");
				instance.initVerify(lastSertifikat(response));
				instance.update(signatureString.getBytes());
				boolean verified = instance.verify(serverSignature);
				if (!verified) {
					throw new ServerSignatureException(response.getStatusLine(), "Melding fra server matcher ikke signatur.");
				} else if (log.isDebugEnabled()) {
					log.debug("Verifiserte signert respons fra Digipost. Signatur fra HTTP-headeren {} var OK: {}", X_Digipost_Signature, base64Encoder.encodeToString(serverSignature));
				}
			} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
				throw new ServerSignatureException(response.getStatusLine(), "Det skjedde en feil under signatursjekk: " + e.getMessage(), e);
			}
		} else if (responseSignatureIsRequired(request)) {
			throw new ServerSignatureException(response.getStatusLine(),
					"Kunne ikke sjekke server-signatur fordi " + X_Digipost_Signature + "-header mangler i respons fra " + request.getRequestLine());
		}
	}

	private boolean skipResponseSignatureVerification(HttpRequest request) {
		if ("/".contentEquals(request.getRequestLine().getUri())) {
			log.debug("Ser ikke etter responssignatur for rot-ressurs {}", request.getRequestLine());
			return true;
		}
		return false;
	}

	private boolean responseSignatureIsRequired(HttpRequest request) {
		RequestLine requestLine = request.getRequestLine();

		if (requestLine.getUri().contains("/user-agreements?") && requestLine.getMethod().contentEquals(HttpGet.METHOD_NAME)) {
			log.debug("Tillater manglende respons-signatur for uthenting av alle user-agreements: {}", requestLine);
			return false;
		}
		return true;
	}




	private Optional<byte[]> findServerSignatureInResponse(final HttpResponse response) {
		String serverSignaturString = null;
		Header firstHeader = response.getFirstHeader(X_Digipost_Signature);
		if (firstHeader != null){
			serverSignaturString = firstHeader.getValue();
		}

		if (isBlank(serverSignaturString)) {
			return Optional.empty();
		}
		return Optional.of(base64Decoder.decode(serverSignaturString));
	}

	private X509Certificate lastSertifikat(HttpResponse response) {
		try {
			InputStream certStream = new ByteArrayInputStream(certificateSupplier.get());

			CertificateFactory cf = CertificateFactory.getInstance("X.509", BouncyCastleProvider.PROVIDER_NAME);
			X509Certificate sertifikat = (X509Certificate) cf.generateCertificate(certStream);
			if (sertifikat == null) {
				throw new ServerSignatureException(response.getStatusLine(),
						"Kunne ikke laste Digipost's public key - server-signatur kunne ikke sjekkes");
			}
			return sertifikat;
		} catch (GeneralSecurityException e) {
			throw new ServerSignatureException(response.getStatusLine(),
					"Kunne ikke laste Digiposts public key - server-signatur kunne ikke sjekkes");
		}
	}
}
