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

import org.apache.http.HttpRequest;
import org.bouncycastle.crypto.ExtendedDigest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.function.Supplier;

public abstract class RequestContentHashFilter {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final Supplier<? extends ExtendedDigest> digestSupplier;
	private final String header;
	private final Base64.Encoder base64Encoder;

	public RequestContentHashFilter(final Supplier<? extends ExtendedDigest> digestSupplier, final String header) {
		this.digestSupplier = digestSupplier;
		this.header = header;
		this.base64Encoder = Base64.getEncoder();
	}

	public void settContentHashHeader(final byte[] data, final HttpRequest httpRequest) {
		ExtendedDigest instance = digestSupplier.get();
		byte[] result = new byte[instance.getDigestSize()];
		instance.update(data, 0, data.length);
		instance.doFinal(result, 0);
		String hash = base64Encoder.encodeToString(result);
		httpRequest.setHeader(header, hash);
		log.debug(RequestContentHashFilter.class.getSimpleName() + " satt headeren " + header + "=" + hash);
	}
}
