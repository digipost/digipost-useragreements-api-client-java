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
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.protocol.HttpContext;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
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

import static no.digipost.api.useragreements.client.Headers.X_Digipost_Signature;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class ResponseSignatureInterceptor implements HttpResponseInterceptor {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final Supplier<byte[]> certificateSupplier;

	public ResponseSignatureInterceptor(final Supplier<byte[]> certificateSupplier) {
		this.certificateSupplier = certificateSupplier;
	}

	@Override
	public void process(HttpResponse response, HttpContext context) {
		if ("/".equals(((CookieOrigin) context.getAttribute("http.cookie-origin")).getPath())) {
			log.debug("Verifiserer ikke signatur fordi det er rotressurs vi hentet.");
			return;
		}

		try {
			String serverSignaturBase64 = getServerSignaturFromResponse(response);
			byte[] serverSignaturBytes = Base64.decode(serverSignaturBase64.getBytes());

			String signatureString = ResponseMessageSignatureUtil.getCanonicalResponseRepresentation(new ClientResponseToVerify(context, response));

			Signature instance = Signature.getInstance("SHA256WithRSAEncryption");
			instance.initVerify(lastSertifikat(response));
			instance.update(signatureString.getBytes());
			boolean verified = instance.verify(serverSignaturBytes);
			if (!verified) {
				throw new ServerSignatureException(response.getStatusLine(), "Melding fra server matcher ikke signatur.");
			} else {
				log.debug("Verifiserte signert respons fra Digipost. Signatur fra HTTP-headeren {} var OK: {}", X_Digipost_Signature, serverSignaturBase64);
			}
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			throw new ServerSignatureException(response.getStatusLine(), "Det skjedde en feil under signatursjekk: " + e.getMessage(), e);
		}
	}

	private String getServerSignaturFromResponse(final HttpResponse response) {
		String serverSignaturString = null;
		Header firstHeader = response.getFirstHeader(X_Digipost_Signature);
		if(firstHeader != null){
			serverSignaturString = firstHeader.getValue();
		}

		if (isBlank(serverSignaturString)) {
			throw new ServerSignatureException(response.getStatusLine(),
					"Mangler " + X_Digipost_Signature + "-header - server-signatur kunne ikke sjekkes");
		}
		return serverSignaturString;
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
