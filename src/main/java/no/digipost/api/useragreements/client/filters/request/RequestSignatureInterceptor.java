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
package no.digipost.api.useragreements.client.filters.request;

import com.google.common.io.ByteStreams;
import no.digipost.api.useragreements.client.Headers;
import no.digipost.api.useragreements.client.security.ClientRequestToSign;
import no.digipost.api.useragreements.client.security.RequestMessageSignatureUtil;
import no.digipost.api.useragreements.client.security.Signer;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class RequestSignatureInterceptor implements HttpRequestInterceptor {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final Signer signer;
	private final RequestContentHashFilter hashFilter;

	public RequestSignatureInterceptor(final Signer signer, final RequestContentHashFilter hashFilter){
		this.signer = signer;
		this.hashFilter = hashFilter;
	}

	private void setSignatureHeader(final HttpRequest httpRequest) {
		String stringToSign = RequestMessageSignatureUtil.getCanonicalRequestRepresentation(new ClientRequestToSign(httpRequest));
		log.debug("String to sign:\n===START SIGNATURSTRENG===\n" + stringToSign
				+ "===SLUTT SIGNATURSTRENG===");

		byte[] signatureBytes = signer.sign(stringToSign);
		String signature = new String(Base64.encode(signatureBytes));
		httpRequest.setHeader(Headers.X_Digipost_Signature, signature);
		log.debug("Settin header " + Headers.X_Digipost_Signature + "=" + signature);
	}

	@Override
	public void process(HttpRequest httpRequest, HttpContext httpContext) throws HttpException, IOException {

		if(httpRequest instanceof HttpEntityEnclosingRequest) {
			HttpEntityEnclosingRequest request = (HttpEntityEnclosingRequest) httpRequest;
			HttpEntity rqEntity = request.getEntity();

			if (rqEntity == null) {
				setSignatureHeader(httpRequest);
			} else {
				byte[] entityBytes = ByteStreams.toByteArray(rqEntity.getContent());
				hashFilter.settContentHashHeader(entityBytes, request);
				setSignatureHeader(httpRequest);
			}
		} else {
			setSignatureHeader(httpRequest);
		}


	}
}
