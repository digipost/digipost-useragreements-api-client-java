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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;
import java.time.Clock;
import java.time.ZonedDateTime;

import static java.time.ZoneOffset.UTC;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.when;

public class ResponseDateInterceptorTest {

	@Rule
	public final MockitoRule mockito = MockitoJUnit.rule();

	@Rule
	public final ExpectedException expectedException = ExpectedException.none();

	@Mock
	private HttpContext httpContextMock;

	@Mock
	private HttpResponse httpResponseMock;

	@Before
	public void setUp() {
		when(httpResponseMock.getStatusLine()).thenReturn(new StatusLineMock(200));
	}

	@Test
	public void skal_kaste_exception_når_Date_header_mangler() throws IOException, HttpException {
		ResponseDateInterceptor interceptor = new ResponseDateInterceptor();
		expectedException.expect(ServerSignatureException.class);
		expectedException.expectMessage(containsString("Respons mangler Date-header"));
		interceptor.process(httpResponseMock, httpContextMock);
	}

	@Test
	public void skal_kaste_feil_når_Date_header_er_på_feil_format() throws IOException, HttpException {
		ResponseDateInterceptor interceptor = new ResponseDateInterceptor();
		when(httpResponseMock.getFirstHeader("Date")).thenReturn(new BasicHeader("Date", "16. januar 2012 - 16:14:23"));
		expectedException.expect(ServerSignatureException.class);
		expectedException.expectMessage(containsString("Date-header kunne ikke parses"));
		interceptor.process(httpResponseMock, httpContextMock);
	}

	@Test
	public void skal_kaste_feil_når_Date_header_er_for_ny() throws IOException, HttpException {
		ResponseDateInterceptor interceptor = new ResponseDateInterceptor(Clock.fixed(ZonedDateTime.of(2014, 11, 4, 21, 00, 58, 0, UTC).toInstant(), UTC));
		when(httpResponseMock.getFirstHeader("Date")).thenReturn(new BasicHeader("Date", "Tue, 04 Nov 2014 21:10:58 GMT"));
		expectedException.expect(ServerSignatureException.class);
		expectedException.expectMessage(containsString("Date-header fra server er for ny"));
		interceptor.process(httpResponseMock, httpContextMock);
	}

	@Test
	public void skal_kaste_feil_når_Date_header_er_for_gammel() throws IOException, HttpException {
		ResponseDateInterceptor interceptor = new ResponseDateInterceptor(Clock.fixed(ZonedDateTime.of(2014, 11, 4, 21, 20, 58, 0, UTC).toInstant(), UTC));
		when(httpResponseMock.getFirstHeader("Date")).thenReturn(new BasicHeader("Date", "Tue, 04 Nov 2014 21:10:58 GMT"));
		expectedException.expect(ServerSignatureException.class);
		expectedException.expectMessage(containsString("Date-header fra server er for gammel"));
		interceptor.process(httpResponseMock, httpContextMock);
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
