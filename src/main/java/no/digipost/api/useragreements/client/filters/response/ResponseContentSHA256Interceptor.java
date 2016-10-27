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
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.util.encoders.Base64;

import java.io.IOException;

import static no.digipost.api.useragreements.client.Headers.X_Content_SHA256;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class ResponseContentSHA256Interceptor implements HttpResponseInterceptor {

	@Override
	public void process(HttpResponse response, HttpContext context) {
		if ("/".equals(((CookieOrigin)(context.getAttribute("http.cookie-origin"))).getPath())) {
			return;
		}

		try {
			final HttpEntity entity = response.getEntity();
			if (entity != null) {
				byte[] entityBytes = IOUtils.toByteArray(entity.getContent());
				EntityUtils.consume(entity);
				if (entityBytes.length > 0) {
					String hashHeader = findHeader(response, X_Content_SHA256);
					if (isBlank(hashHeader)) {
						throw new ServerSignatureException(response.getStatusLine(), "Mangler X-Content-SHA256-header - server-signatur kunne ikke valideres");
					}
					validerBytesMotHashHeader(hashHeader, entityBytes, response);
				}
				response.setEntity(new ByteArrayEntity(entityBytes));
			}
		} catch (IOException e) {
			throw new ServerSignatureException(response.getStatusLine(), "Det skjedde en feil under uthenting av innhold for validering av X-Content-SHA256-header - server-signatur kunne ikke valideres: " + e.getMessage());
		}
	}

	private String findHeader(final HttpResponse response, final String header) {
		String hashHeader = null;
		for(Header head : response.getAllHeaders()){
			if(head.getName().equals(header)){
				hashHeader = head.getValue();
				break;
			}
		}
		return hashHeader;
	}

	private void validerBytesMotHashHeader(final String serverHash, final byte[] entityBytes, HttpResponse response) {
		SHA256Digest digest = new SHA256Digest();

		digest.update(entityBytes, 0, entityBytes.length);
		byte[] result = new byte[digest.getDigestSize()];
		digest.doFinal(result, 0);
		String ourHash = new String(Base64.encode(result));
		if (!serverHash.equals(ourHash)) {
			throw new ServerSignatureException(response.getStatusLine(), "X-Content-SHA256-header matchet ikke innholdet - server-signatur er feil.");
		}
	}
}
