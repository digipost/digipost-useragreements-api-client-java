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
package no.digipost.api.useragreements.client;

import no.digipost.api.useragreements.client.filters.request.RequestContentSHA256Filter;
import no.digipost.api.useragreements.client.filters.request.RequestDateInterceptor;
import no.digipost.api.useragreements.client.filters.request.RequestSignatureInterceptor;
import no.digipost.api.useragreements.client.filters.request.RequestUserAgentInterceptor;
import no.digipost.api.useragreements.client.filters.response.ResponseContentSHA256Interceptor;
import no.digipost.api.useragreements.client.filters.response.ResponseDateInterceptor;
import no.digipost.api.useragreements.client.filters.response.ResponseSignatureInterceptor;
import no.digipost.api.useragreements.client.security.CryptoUtil;
import no.digipost.api.useragreements.client.security.PrivateKeySigner;
import no.digipost.api.useragreements.client.util.Supplier;
import no.digipost.http.client.DigipostHttpClientFactory;
import no.digipost.http.client.DigipostHttpClientSettings;
import org.apache.http.*;
import org.apache.http.client.ResponseHandler;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.joda.time.LocalDate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXB;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.PrivateKey;
import java.util.List;
import java.util.Objects;

/**
 * API client for managing Digipost documents on behalf of users
 */
public class DigipostUserAgreementsClient {

	private final ApiService apiService;

	public DigipostUserAgreementsClient(final ApiService apiService) {
		this.apiService = apiService;
		CryptoUtil.verifyTLSCiphersAvailable();
	}

	public IdentificationResult identifyUser(final SenderId senderId, final UserId userId) {
		return identifyUser(senderId, userId, null); }

	public IdentificationResult identifyUser(final SenderId senderId, final UserId userId, final String requestTrackingId) {
		Objects.requireNonNull(senderId, "senderId cannot be null");
		Objects.requireNonNull(userId, "userId cannot be null");
		return apiService.identifyUser(senderId, userId, requestTrackingId, simpleJAXBEntityHandler(IdentificationResult.class));
	}

	public URI createOrReplaceAgreement(final SenderId senderId, final Agreement agreement) {
		return createOrReplaceAgreement(senderId, agreement, null); }

	public URI createOrReplaceAgreement(final SenderId senderId, final Agreement agreement, final String requestTrackingId) {
		Objects.requireNonNull(senderId, "senderId cannot be null");
		Objects.requireNonNull(agreement, "agreement cannot be null");
		return apiService.createAgreement(senderId, agreement, requestTrackingId, createdWithLocationHandler());
	}

	public Agreement getAgreement(final URI agreementUri, final String requestTrackingId) {
		return apiService.getAgreement(agreementUri, requestTrackingId, simpleJAXBEntityHandler(Agreement.class));
	}

	public GetAgreementResult getAgreement(final SenderId senderId, final AgreementType type, final UserId userId, final String requestTrackingId) {
		Objects.requireNonNull(senderId, "senderId cannot be null");
		Objects.requireNonNull(type, "agreementType cannot be null");
		Objects.requireNonNull(userId, "userId cannot be null");
		return apiService.getAgreement(senderId, type, userId, requestTrackingId, new ResponseHandler<GetAgreementResult>() {
			@Override
			public GetAgreementResult handleResponse(final HttpResponse response) throws IOException {
				final StatusLine status = response.getStatusLine();

				if (status.getStatusCode() == HttpStatus.SC_OK) {
					return new GetAgreementResult(unmarshallEntity(response, Agreement.class));
				} else if (status.getStatusCode() == HttpStatus.SC_NOT_FOUND){
					final Error error = readErrorFromResponse(response);
					final Supplier<UnexpectedResponseException> agreementMissingExceptionSupplier = new Supplier<UnexpectedResponseException>() {
						@Override
						public UnexpectedResponseException get() {
							return new UnexpectedResponseException(status, error);
						}
					};
					if (error.hasCode(ErrorCode.UNKNOWN_USER_ID)) {
						return new GetAgreementResult(GetAgreementResult.FailedReason.UNKNOWN_USER, agreementMissingExceptionSupplier);
					} else if (error.hasCode(ErrorCode.AGREEMENT_NOT_FOUND)) {
						return new GetAgreementResult(GetAgreementResult.FailedReason.NO_AGREEMENT, agreementMissingExceptionSupplier);
					} else {
						throw new UnexpectedResponseException(status, error);
					}
				} else {
					throw new UnexpectedResponseException(status, readErrorFromResponse(response));
				}
			}
		});
	}

	public List<Agreement> getAgreements(final SenderId senderId, final UserId userId) {
		return getAgreements(senderId, userId, null);
	}

	public List<Agreement> getAgreements(final SenderId senderId, final UserId userId, final String requestTrackingId) {
		final Agreements agreements = apiService.getAgreements(senderId, userId, requestTrackingId, simpleJAXBEntityHandler(Agreements.class));
		return agreements.getAgreements();
	}

	public void deleteAgreement(final URI agreementPath, final String requestTrackingId) {
		apiService.deleteAgrement(agreementPath, requestTrackingId, voidOkHandler());
	}

	public void deleteAgreement(final SenderId senderId, final AgreementType agreementType, final UserId userId, final String requestTrackingId) {
		apiService.deleteAgrement(senderId, agreementType, userId, requestTrackingId, voidOkHandler());
	}

	@Deprecated
	public List<Document> getDocuments(final SenderId senderId, final AgreementType agreementType, final UserId userId, final InvoiceStatus status, final LocalDate invoiceDueDateFrom) {
		return getDocuments(senderId, agreementType, userId, GetDocumentsQuery.builder().invoiceStatus(status).invoiceDueDateFrom(invoiceDueDateFrom).build(), null);
	}

	@Deprecated
	public List<Document> getDocuments(final SenderId senderId, final AgreementType agreementType, final UserId userId, final InvoiceStatus status, final LocalDate invoiceDueDateFrom, final String requestTrackingId) {
		return getDocuments(senderId, agreementType, userId, GetDocumentsQuery.builder().invoiceStatus(status).invoiceDueDateFrom(invoiceDueDateFrom).build(), requestTrackingId);
	}

	public List<Document> getDocuments(final SenderId senderId, final AgreementType agreementType, final UserId userId, final GetDocumentsQuery query) {
		return getDocuments(senderId, agreementType, userId, query, null);
	}

	public List<Document> getDocuments(final SenderId senderId, final AgreementType agreementType, final UserId userId, final GetDocumentsQuery query, final String requestTrackingId) {
		Objects.requireNonNull(senderId, "senderId cannot be null");
		Objects.requireNonNull(agreementType, "agreementType cannot be null");
		Objects.requireNonNull(userId, "userId cannot be null");
		final Documents documents = apiService.getDocuments(senderId, agreementType, userId, query, requestTrackingId, simpleJAXBEntityHandler(Documents.class));
		return documents.getDocuments();
	}

	public Document getDocument(final SenderId senderId, final AgreementType agreementType, final long documentId) {
		return getDocument(senderId, agreementType, documentId, null);
	}

	public Document getDocument(final SenderId senderId, final AgreementType agreementType, final long documentId, final String requestTrackingId) {
		return apiService.getDocument(senderId, agreementType, documentId, requestTrackingId, simpleJAXBEntityHandler(Document.class));
	}

	public void payInvoice(final SenderId senderId, final AgreementType agreementType, final long documentId, final InvoicePayment invoicePayment) {
		payInvoice(senderId, agreementType, documentId, invoicePayment, null);
	}

	public void payInvoice(final SenderId senderId, final AgreementType agreementType, final long documentId, final InvoicePayment invoicePayment, final String requestTrackingId) {
		apiService.updateInvoice(senderId, agreementType, documentId, invoicePayment.asInvoiceUpdate(), requestTrackingId, voidOkHandler());
	}

	public void updateInvoice(final SenderId senderId, final AgreementType agreementType, final long documentId, final InvoiceUpdate invoiceUpdate) {
		updateInvoice(senderId, agreementType, documentId, invoiceUpdate, null);
	}

	public void updateInvoice(final SenderId senderId, final AgreementType agreementType, final long documentId, final InvoiceUpdate invoiceUpdate, final String requestTrackingId) {
		apiService.updateInvoice(senderId, agreementType, documentId, invoiceUpdate, requestTrackingId, voidOkHandler());
	}

	public void deleteInvoice(final SenderId senderId, final AgreementType agreementType, final long documentId) {
		deleteInvoice(senderId, agreementType, documentId, null);
	}

	public void deleteInvoice(final SenderId senderId, final AgreementType agreementType, final long documentId, final String requestTrackingId) {
		apiService.updateInvoice(senderId, agreementType, documentId, new InvoiceUpdate(InvoiceStatus.DELETED), requestTrackingId, voidOkHandler());
	}

	@Deprecated
	public long getDocumentCount(final SenderId senderId, final AgreementType agreementType, final UserId userId, final InvoiceStatus status, final LocalDate invoiceDueDateFrom) {
		return getDocumentCount(senderId, agreementType, userId, GetDocumentsQuery.builder().invoiceStatus(status).invoiceDueDateFrom(invoiceDueDateFrom).build(), null);
	}

	@Deprecated
	public long getDocumentCount(final SenderId senderId, final AgreementType agreementType, final UserId userId, final InvoiceStatus status, final LocalDate invoiceDueDateFrom, final String requestTrackingId) {
		return getDocumentCount(senderId, agreementType, userId, GetDocumentsQuery.builder().invoiceStatus(status).invoiceDueDateFrom(invoiceDueDateFrom).build(), requestTrackingId);
	}

	public long getDocumentCount(final SenderId senderId, final AgreementType agreementType, final UserId userId, final GetDocumentsQuery query) {
		return getDocumentCount(senderId, agreementType, userId, query, null);
	}

	public long getDocumentCount(final SenderId senderId, final AgreementType agreementType, final UserId userId, final GetDocumentsQuery query, final String requestTrackingId) {
		Objects.requireNonNull(senderId, "senderId cannot be null");
		Objects.requireNonNull(agreementType, "agreementType cannot be null");
		Objects.requireNonNull(userId, "userId cannot be null");
		return apiService.getDocumentCount(senderId, agreementType, userId, query, requestTrackingId, simpleJAXBEntityHandler(DocumentCount.class)).getCount();
	}

	public DocumentContent getDocumentContent(final SenderId senderId, final AgreementType agreementType, final long documentId) {
		return getDocumentContent(senderId, agreementType, documentId, null);
	}

	public DocumentContent getDocumentContent(final SenderId senderId, final AgreementType agreementType, final long documentId, final String requestTrackingId) {
		return apiService.getDocumentContent(senderId, agreementType, documentId, requestTrackingId, simpleJAXBEntityHandler(DocumentContent.class));
	}

	public List<UserId> getAgreementUsers(final SenderId senderId, final AgreementType agreementType, final Boolean smsNotificationEnabled) {
		return getAgreementUsers(senderId, agreementType, smsNotificationEnabled, null);
	}

	public List<UserId> getAgreementUsers(final SenderId senderId, final AgreementType agreementType, final Boolean smsNotificationEnabled, final String requestTrackingId) {
		Objects.requireNonNull(senderId, "senderId cannot be null");
		Objects.requireNonNull(agreementType, "agreementType cannot be null");
		final AgreementUsers agreementUsers = apiService.getAgreementUsers(senderId, agreementType, smsNotificationEnabled, requestTrackingId, simpleJAXBEntityHandler(AgreementUsers.class));
		return agreementUsers.getUsers();
	}

	private ResponseHandler<Void> voidOkHandler() {
		return new ResponseHandler<Void>() {
			@Override
			public Void handleResponse(final HttpResponse response) throws IOException {
				final StatusLine statusLine = response.getStatusLine();
				if (isOkResponse(statusLine.getStatusCode())) {
					return null;
				} else {
					throw new UnexpectedResponseException(statusLine, readErrorFromResponse(response));
				}
			}
		};
	}

	private ResponseHandler<URI> createdWithLocationHandler() {
		return new ResponseHandler<URI>() {
			@Override
			public URI handleResponse(final HttpResponse response) throws IOException {
				final StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() == HttpStatus.SC_CREATED) {
					try {
						return new URI(response.getFirstHeader(HttpHeaders.LOCATION).getValue());
					} catch (URISyntaxException e) {
						throw new UnexpectedResponseException(statusLine, ErrorCode.GENERAL_ERROR, "Invalid location header", e);
					}
				} else {
					throw new UnexpectedResponseException(statusLine, readErrorFromResponse(response));
				}
			}
		};
	}

	private <T> ResponseHandler<T> simpleJAXBEntityHandler(final Class<T> responseType){
		return new ResponseHandler<T>() {
			@Override
			public T handleResponse(final HttpResponse response) throws IOException {
				final StatusLine statusLine = response.getStatusLine();
				if (isOkResponse(statusLine.getStatusCode())) {
					return JAXB.unmarshal(response.getEntity().getContent(), responseType);
				} else {
					throw new UnexpectedResponseException(statusLine, readErrorFromResponse(response));
				}
			}
		};
	}

	public static boolean isOkResponse(final int status) {
		return status / 100 == 2;
	}

	public static <T> T unmarshallEntity(final HttpResponse response, final Class<T> returnType) {
		final StatusLine statusLine = response.getStatusLine();
		try {
			final String body = EntityUtils.toString(response.getEntity());
			if (body.length() == 0) {
				throw new UnexpectedResponseException(statusLine, ErrorCode.NO_ENTITY, "Message body is empty");
			}
			try {
				T result = JAXB.unmarshal(response.getEntity().getContent(), returnType);
				return result;
			} catch (IllegalStateException | DataBindingException e) {
				throw new UnexpectedResponseException(statusLine, ErrorCode.GENERAL_ERROR, body, e);
			}
		} catch (IOException e) {
			throw new UnexpectedResponseException(statusLine, ErrorCode.IO_EXCEPTION, e.getMessage(), e);
		}
	}

	public static Error readErrorFromResponse(final HttpResponse response) {
		return unmarshallEntity(response, Error.class);
	}

	public static class Builder {

		private static final URI PRODUCTION_ENDPOINT = URI.create("https://api.digipost.no");

		private URI serviceEndpoint;
		private final BrokerId brokerId;
		private final InputStream certificateP12File;
		private final String certificatePassword;
		private HttpClientBuilder httpClientBuilder;
		private HttpHost proxyHost;
		private PrivateKey privateKey;

		public Builder(final BrokerId brokerId, InputStream certificateP12File, String certificatePassword){
			this(brokerId, certificateP12File, certificatePassword, null);
		}

		public Builder(BrokerId brokerId, PrivateKey privateKey) {
			this(brokerId, null, null, privateKey);
		}

		private Builder(BrokerId brokerId, InputStream certificateP12File, String certificatePassword, PrivateKey privateKey) {
			this.brokerId = brokerId;
			if (privateKey == null && (certificateP12File == null || certificatePassword == null)) {
				throw new IllegalArgumentException("Client must be supplied either PrivateKey, or Certificate and password for certificate");
			}
			this.certificateP12File = certificateP12File;
			this.certificatePassword = certificatePassword;
			this.privateKey = privateKey;
			serviceEndpoint(PRODUCTION_ENDPOINT);
			httpClientBuilder = DigipostHttpClientFactory.createBuilder(DigipostHttpClientSettings.DEFAULT);
		}

		public Builder useProxy(final HttpHost proxyHost) {
			this.proxyHost = proxyHost;
			return this;
		}

		public Builder serviceEndpoint(URI endpointUri) {
			this.serviceEndpoint = endpointUri;
			return this;
		}

        public Builder setHttpClientBuilder(final HttpClientBuilder HttpClientBuilder) {
            this.httpClientBuilder = httpClientBuilder;
            return this;
        }

		public Builder veryDangerouslyDisableCertificateVerificationWhichIsAbsolutelyUnfitForProductionCode() {
			if (this.serviceEndpoint.compareTo(PRODUCTION_ENDPOINT) == 0) {
				throw new RuntimeException("You should never ever disable certificate verification when connecting to the production endpoint");
			}
			SSLContextBuilder sslContextBuilder= new SSLContextBuilder();
			try {
				sslContextBuilder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
				SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContextBuilder.build(), new HostnameVerifier() {
					@Override
					public boolean verify(String s, SSLSession sslSession) {
						return true;
					}
				});
				httpClientBuilder.setSSLSocketFactory(sslConnectionSocketFactory);
			} catch (Exception e) {
				throw new RuntimeException("Could not disable certificate verification: " + e);
			}
			System.err.println("Not checking validity of certificates for any hostnames");
			return this;
		}

		public DigipostUserAgreementsClient build() {
			final ApiServiceProvider apiServiceProvider = new ApiServiceProvider();
			final ResponseSignatureInterceptor responseSignatureInterceptor = new ResponseSignatureInterceptor(new Supplier<byte[]>() {
				@Override
				public byte[] get() {
					return apiServiceProvider.getApiService().getEntryPoint().getCertificate().getBytes();
				}
			});

			httpClientBuilder.addInterceptorLast(new RequestDateInterceptor());
			httpClientBuilder.addInterceptorLast(new RequestUserAgentInterceptor());
			if (privateKey == null) {
				httpClientBuilder.addInterceptorLast(new RequestSignatureInterceptor(new PrivateKeySigner(certificateP12File, certificatePassword), new RequestContentSHA256Filter()));
			} else {
				httpClientBuilder.addInterceptorLast(new RequestSignatureInterceptor(new PrivateKeySigner(privateKey), new RequestContentSHA256Filter()));
			}
			httpClientBuilder.addInterceptorLast(new ResponseDateInterceptor());
			httpClientBuilder.addInterceptorLast(new ResponseContentSHA256Interceptor());
			httpClientBuilder.addInterceptorLast(responseSignatureInterceptor);

			if (proxyHost != null) {
				httpClientBuilder.setProxy(proxyHost);
			}

			final ApiService apiService = new ApiService(serviceEndpoint, brokerId, httpClientBuilder.build());
			apiServiceProvider.setApiService(apiService);
			return new DigipostUserAgreementsClient(apiService);
		}
	}
}