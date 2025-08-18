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

import no.digipost.api.useragreements.client.util.DateUtils;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.ZonedDateTime;

import static no.digipost.api.useragreements.client.util.DateUtils.GMT;
import static org.apache.hc.core5.http.HttpHeaders.DATE;

public class RequestDateInterceptor implements HttpRequestInterceptor {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public void process(HttpRequest httpRequest, EntityDetails entityDetails, HttpContext httpContext) throws HttpException, IOException {
		setDateHeader(httpRequest);
	}

	private void setDateHeader(final HttpRequest httpRequest) {
		String dateOnRFC1123Format = DateUtils.formatDate(ZonedDateTime.now(GMT));
		httpRequest.setHeader(DATE, dateOnRFC1123Format);
		log.debug(getClass().getSimpleName() + " satt headeren " + DATE + "=" + dateOnRFC1123Format);
	}
}
