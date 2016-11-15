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
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.joda.time.DateTimeZone.UTC;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ResponseDateInterceptorTest {

	private ResponseDateInterceptor responseDateInterceptor;

	@Mock
	private HttpContext httpContextMock;

	@Mock
	private HttpResponse httpResponseMock;

	@Before
	public void setUp() {
		responseDateInterceptor = new ResponseDateInterceptor();
		when(httpResponseMock.getStatusLine()).thenReturn(new StatusLineMock(200));
	}

	@Test
	public void skal_kaste_exception_når_Date_header_mangler() throws IOException, HttpException {
		when(httpResponseMock.getAllHeaders()).thenReturn(new BasicHeader[]{});
		try {
			responseDateInterceptor.process(httpResponseMock, httpContextMock);
			fail("Skulle ha kastet feil grunnet manglende Date-header");
		} catch (ServerSignatureException e) {
			assertThat(e.getMessage(), containsString("Respons mangler Date-header"));
		}
	}

	@Test
	public void skal_kaste_custom_exception_når_Date_header_mangler() throws IOException, HttpException {
		when(httpResponseMock.getAllHeaders()).thenReturn(new BasicHeader[]{});
		try {
			ResponseDateInterceptor responseDateInterceptor = new ResponseDateInterceptor();
			responseDateInterceptor.process(httpResponseMock, httpContextMock);
			fail("Skulle ha kastet feil grunnet manglende Date-header");
		} catch (ServerSignatureException e) {
			assertThat(e.getMessage(), containsString("Respons mangler Date-header"));
		}
	}

	@Test
	public void skal_kaste_feil_når_Date_header_er_på_feil_format() throws IOException, HttpException {
		List<BasicHeader> headers = new ArrayList<>();
		headers.add(new BasicHeader("Date", "16. januar 2012 - 16:14:23"));
		when(httpResponseMock.getAllHeaders()).thenReturn(headers.toArray(new BasicHeader[0]));
		when(httpResponseMock.getFirstHeader("Date")).thenReturn(headers.get(0));
		try {
			responseDateInterceptor.process(httpResponseMock, httpContextMock);
			fail("Skulle kastet feil grunnet feilaktig Date header format");
		} catch (ServerSignatureException e) {
			assertThat(e.getMessage(), containsString("Date-header kunne ikke parses"));
		}
	}

	@Test
	public void skal_kaste_feil_når_Date_header_er_for_ny() throws IOException, HttpException {
		DateTimeUtils.setCurrentMillisFixed(new DateTime(2014, 11, 4, 21, 00, 58, UTC).getMillis());
		List<BasicHeader> headers = new ArrayList<>();
		headers.add(new BasicHeader("Date", "Tue, 04 Nov 2014 21:10:58 GMT"));
		when(httpResponseMock.getAllHeaders()).thenReturn(headers.toArray(new BasicHeader[0]));
		when(httpResponseMock.getFirstHeader("Date")).thenReturn(headers.get(0));
		try {
			responseDateInterceptor.process(httpResponseMock, httpContextMock);
			fail("Skulle kastet feil grunnet for ny Date header");
		} catch (ServerSignatureException e) {
			assertThat(e.getMessage(), containsString("Date-header fra server er for ny"));
		}
	}

	@Test
	public void skal_kaste_feil_når_Date_header_er_for_gammel() throws IOException, HttpException {
		DateTimeUtils.setCurrentMillisFixed(new DateTime(2014, 11, 4, 21, 20, 58, UTC).getMillis());
		List<BasicHeader> headers = new ArrayList<>();
		headers.add(new BasicHeader("Date", "Tue, 04 Nov 2014 21:10:58 GMT"));
		when(httpResponseMock.getAllHeaders()).thenReturn(headers.toArray(new BasicHeader[0]));
		when(httpResponseMock.getFirstHeader("Date")).thenReturn(headers.get(0));
		try {
			responseDateInterceptor.process(httpResponseMock, httpContextMock);
			fail("Skulle kastet feil grunnet for gammel Date header");
		} catch (ServerSignatureException e) {
			assertThat(e.getMessage(), containsString("Date-header fra server er for gammel"));
		}
	}

	public static class StatusLineMock implements StatusLine {

		private final int statusCode;
		public StatusLineMock(int statusCode){
			this.statusCode = statusCode;
		}

		@Override
		public ProtocolVersion getProtocolVersion() {
			return null;
		}

		@Override
		public int getStatusCode() {
			return statusCode;
		}

		@Override
		public String getReasonPhrase() {
			return null;
		}
	}
}