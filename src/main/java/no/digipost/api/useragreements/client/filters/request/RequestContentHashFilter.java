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

import no.digipost.api.useragreements.client.ErrorCode;
import no.digipost.api.useragreements.client.UserAgreementsApiException;
import org.apache.http.HttpRequest;
import org.bouncycastle.crypto.ExtendedDigest;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RequestContentHashFilter {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final Class<? extends ExtendedDigest> digestClass;
	private final String header;

	public RequestContentHashFilter(final Class<? extends ExtendedDigest> digestClass, final String header) {
		this.digestClass = digestClass;
		this.header = header;
	}

	public void settContentHashHeader(final byte[] data, final HttpRequest httpRequest) {
		try {
			ExtendedDigest instance = digestClass.newInstance();
			byte[] result = new byte[instance.getDigestSize()];
			instance.update(data, 0, data.length);
			instance.doFinal(result, 0);
			String hash = new String(Base64.encode(result));
			httpRequest.setHeader(header, hash);
			log.debug(RequestContentHashFilter.class.getSimpleName() + " satt headeren " + header + "=" + hash);
		} catch (InstantiationException | IllegalAccessException e) {
			throw new UserAgreementsApiException(ErrorCode.CLIENT_TECHNICAL_ERROR, "Feil ved generering av " + header, e);
		}
	}
}
