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

import no.digipost.api.useragreements.client.ApiService;
import no.digipost.api.useragreements.client.ServerSignatureException;
import no.digipost.api.useragreements.client.filters.response.ResponseDateInterceptorTest.StatusLineMock;
import no.digipost.api.useragreements.client.util.Supplier;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.HttpContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class ResponseSignatureFilterTest {

	private ResponseSignatureInterceptor responseSignatureInterceptor;

	@Rule
	public final MockitoRule mockito = MockitoJUnit.rule();

	@Mock
	private ApiService apiServiceMock;

	@Mock
	private HttpResponse httpResponseMock;

	@Mock
	private HttpContext httpContextMock;

	@Before
	public void setUp() throws URISyntaxException {
		responseSignatureInterceptor = new ResponseSignatureInterceptor(new Supplier<byte[]>() {
			@Override
			public byte[] get() {
				return apiServiceMock.getEntryPoint().getCertificate().getBytes();
			}
		});
		when(httpContextMock.getAttribute(anyString())).thenReturn(new HttpGet("http://host/some/resource"));
		when(httpResponseMock.getStatusLine()).thenReturn(new StatusLineMock(200));
	}

	@Test
	public void skal_kaste_feil_om_server_signatur_mangler() throws IOException, HttpException {
		try {
			responseSignatureInterceptor.process(httpResponseMock, httpContextMock);
			fail("Skulle kastet feil grunnet manglende signatur header");
		} catch (ServerSignatureException e) {
			assertThat(e.getMessage(), containsString("X-Digipost-Signature-header mangler"));
		}
	}

	@Test
	public void skal_kaste_custom_feil_om_server_signatur_mangler() throws IOException, HttpException {
		try {
			ResponseSignatureInterceptor responseSignatureInterceptor = new ResponseSignatureInterceptor(new Supplier<byte[]>() {
				@Override
				public byte[] get() {
					return apiServiceMock.getEntryPoint().getCertificate().getBytes();
				}
			});
			responseSignatureInterceptor.process(httpResponseMock, httpContextMock);
			fail("Skulle kastet feil grunnet manglende signatur header");
		} catch (ServerSignatureException e) {
			assertThat(e.getMessage(), containsString("X-Digipost-Signature-header mangler"));
		}
	}

}
