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
import no.digipost.api.useragreements.client.util.DateUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.protocol.HttpContext;
import org.joda.time.DateTime;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.http.HttpHeaders.DATE;
import static org.joda.time.DateTime.now;

public class ResponseDateInterceptor implements HttpResponseInterceptor {

	private static final int AKSEPTABEL_TIDSDIFFERANSE_MINUTTER = 5;

	@Override
	public void process(HttpResponse response, HttpContext context) {
		String dateHeader = null;
		Header firstHeader = response.getFirstHeader(DATE);
		if(firstHeader != null){
			dateHeader = firstHeader.getValue();
		}

		if (isNotBlank(dateHeader)) {
			sjekkDato(dateHeader, response);
		} else {
			throw new ServerSignatureException(response.getStatusLine(), "Respons mangler Date-header");
		}
	}

	private void sjekkDato(final String dateOnRFC1123Format, HttpResponse response) {
		try {
			DateTime date = DateUtils.parseDate(dateOnRFC1123Format);
			sjekkAtDatoHeaderIkkeErForGammel(dateOnRFC1123Format, date, response);
			sjekkAtDatoHeaderIkkeErForNy(dateOnRFC1123Format, date, response);
		} catch (IllegalArgumentException e) {
			throw new ServerSignatureException(response.getStatusLine(), "Date-header kunne ikke parses: " + e.getMessage(), e);
		}
	}

	private void sjekkAtDatoHeaderIkkeErForGammel(final String headerDate, final DateTime parsedDate, HttpResponse response) {
		if (parsedDate.isBefore(now().minusMinutes(AKSEPTABEL_TIDSDIFFERANSE_MINUTTER))) {
			throw new ServerSignatureException(response.getStatusLine(), "Date-header fra server er for gammel: " + headerDate);
		}
	}

	private void sjekkAtDatoHeaderIkkeErForNy(final String headerDate, final DateTime parsedDate, HttpResponse response) {
		if (parsedDate.isAfter(now().plusMinutes(AKSEPTABEL_TIDSDIFFERANSE_MINUTTER))) {
			throw new ServerSignatureException(response.getStatusLine(), "Date-header fra server er for ny: " + headerDate);
		}
	}
}
