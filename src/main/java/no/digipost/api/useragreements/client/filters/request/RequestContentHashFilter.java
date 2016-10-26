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

import no.digipost.api.useragreements.client.EventLogger;
import org.apache.http.HttpRequest;
import org.bouncycastle.crypto.ExtendedDigest;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static no.digipost.api.useragreements.client.util.MigrationUtil.NOOP_EVENT_LOGGER;

public abstract class RequestContentHashFilter {

	private static final Logger LOG = LoggerFactory.getLogger(RequestContentHashFilter.class);
	private final EventLogger eventLogger;
	private final Class<? extends ExtendedDigest> digestClass;
	private final String header;

	public RequestContentHashFilter(final EventLogger eventListener, final Class<? extends ExtendedDigest> digestClass, final String header) {
		eventLogger = eventListener != null ? eventListener : NOOP_EVENT_LOGGER;
		this.digestClass = digestClass;
		this.header = header;
	}

	public RequestContentHashFilter(final Class<? extends ExtendedDigest> digestClass, final String header) {
		this(NOOP_EVENT_LOGGER, digestClass, header);
	}

	private void log(final String stringToSignMsg) {
		LOG.debug(stringToSignMsg);
		eventLogger.log(stringToSignMsg);
	}

	public void settContentHashHeader(final byte[] data, final HttpRequest httpRequest) {
		try {
			ExtendedDigest instance = digestClass.newInstance();
			byte[] result = new byte[instance.getDigestSize()];
			instance.update(data, 0, data.length);
			instance.doFinal(result, 0);
			String hash = new String(Base64.encode(result));
			httpRequest.setHeader(header, hash);
			log(RequestContentHashFilter.class.getSimpleName() + " satt headeren " + header + "=" + hash);
		} catch (InstantiationException | IllegalAccessException e) {
			log("Feil ved generering av " + header + " : " + e.getMessage());
		}
	}
}
