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
import no.digipost.api.useragreements.client.filters.response.ResponseDateInterceptor;
import no.digipost.api.useragreements.client.security.CryptoUtil;
import no.digipost.api.useragreements.client.security.PrivateKeySigner;
import no.digipost.http.client3.DigipostHttpClientFactory;
import no.digipost.http.client3.DigipostHttpClientSettings;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ResponseHandler;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;

import java.io.InputStream;
import java.net.URI;
import java.security.PrivateKey;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static no.digipost.api.useragreements.client.util.ResponseUtils.isOkResponse;
import static no.digipost.api.useragreements.client.util.ResponseUtils.mapOkResponseOrThrowException;
import static no.digipost.api.useragreements.client.util.ResponseUtils.readErrorEntity;
import static no.digipost.api.useragreements.client.util.ResponseUtils.unmarshallEntity;

/**
 * API client for managing Digipost documents on behalf of users
 */
public class DigipostUserAgreementsClient {

	static {
		CryptoUtil.addBouncyCastleProviderAndVerify_AES256_CBC_Support();
	}

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
		return apiService.identifyUser(senderId, userId, requestTrackingId, singleJaxbEntityHandler(IdentificationResult.class));
	}

	public void createOrReplaceAgreement(final SenderId senderId, final Agreement agreement) {
		createOrReplaceAgreement(senderId, agreement, null); }

	public void createOrReplaceAgreement(final SenderId senderId, final Agreement agreement, final String requestTrackingId) {
		Objects.requireNonNull(senderId, "senderId cannot be null");
		Objects.requireNonNull(agreement, "agreement cannot be null");
		apiService.createAgreement(senderId, agreement, requestTrackingId, voidOkHandler());
	}

	public GetAgreementResult getAgreement(final SenderId senderId, final AgreementType type, final UserId userId) {
		return getAgreement(senderId, type, userId, null);
	}

	public GetAgreementResult getAgreement(final SenderId senderId, final AgreementType type, final UserId userId, final String requestTrackingId) {
		Objects.requireNonNull(senderId, "senderId cannot be null");
		Objects.requireNonNull(type, "agreementType cannot be null");
		Objects.requireNonNull(userId, "userId cannot be null");
		return apiService.getAgreement(senderId, type, userId, requestTrackingId, response -> {
			StatusLine status = response.getStatusLine();
			if (isOkResponse(status.getStatusCode())) {
				return new GetAgreementResult(unmarshallEntity(response, Agreements.class).getSingleAgreement());
			} else {
				final Error error = readErrorEntity(response);
				if (status.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
					if (error.hasCode(ErrorCode.UNKNOWN_USER_ID)) {
						return new GetAgreementResult(GetAgreementResult.FailedReason.UNKNOWN_USER, () -> new UnexpectedResponseException(status, error));
					} else if (error.hasCode(ErrorCode.AGREEMENT_NOT_FOUND)) {
						return new GetAgreementResult(GetAgreementResult.FailedReason.NO_AGREEMENT, () -> new UnexpectedResponseException(status, error));
					}
				}
				throw new UnexpectedResponseException(status, error);
			}
		});
	}

	public Stream<Agreement> getAgreementsOfType(final SenderId senderId, final AgreementType agreementType) {
		return getAgreementsOfType(senderId, agreementType, null);
	}

	public Stream<Agreement> getAgreementsOfType(final SenderId senderId, final AgreementType agreementType, String requestTrackingId) {
		return apiService.getAgreementsOfType(senderId, agreementType, requestTrackingId);
	}

	public List<Agreement> getAgreements(final SenderId senderId, final UserId userId) {
		return getAgreements(senderId, userId, null);
	}

	public List<Agreement> getAgreements(final SenderId senderId, final UserId userId, final String requestTrackingId) {
		return apiService.getAgreements(senderId, userId, requestTrackingId, singleJaxbEntityHandler(Agreements.class)).getAgreements();
	}

	public void deleteAgreement(final SenderId senderId, final AgreementType agreementType, final UserId userId) {
		deleteAgreement(senderId, agreementType, userId, null);
	}

	public void deleteAgreement(final SenderId senderId, final AgreementType agreementType, final UserId userId, final String requestTrackingId) {
		apiService.deleteAgrement(senderId, agreementType, userId, requestTrackingId, voidOkHandler());
	}

	public List<Document> getDocuments(final SenderId senderId, final AgreementType agreementType, final UserId userId, final GetDocumentsQuery query) {
		return getDocuments(senderId, agreementType, userId, query, null);
	}

	public List<Document> getDocuments(final SenderId senderId, final AgreementType agreementType, final UserId userId, final GetDocumentsQuery query, final String requestTrackingId) {
		Objects.requireNonNull(senderId, "senderId cannot be null");
		Objects.requireNonNull(agreementType, "agreementType cannot be null");
		Objects.requireNonNull(userId, "userId cannot be null");
		final Documents documents = apiService.getDocuments(senderId, agreementType, userId, query, requestTrackingId, singleJaxbEntityHandler(Documents.class));
		return documents.getDocuments();
	}

	public Document getDocument(final SenderId senderId, final AgreementType agreementType, final long documentId) {
		return getDocument(senderId, agreementType, documentId, null);
	}

	public Document getDocument(final SenderId senderId, final AgreementType agreementType, final long documentId, final String requestTrackingId) {
		return apiService.getDocument(senderId, agreementType, documentId, requestTrackingId, singleJaxbEntityHandler(Document.class));
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

	public long getDocumentCount(final SenderId senderId, final AgreementType agreementType, final UserId userId, final GetDocumentsQuery query) {
		return getDocumentCount(senderId, agreementType, userId, query, null);
	}

	public long getDocumentCount(final SenderId senderId, final AgreementType agreementType, final UserId userId, final GetDocumentsQuery query, final String requestTrackingId) {
		Objects.requireNonNull(senderId, "senderId cannot be null");
		Objects.requireNonNull(agreementType, "agreementType cannot be null");
		Objects.requireNonNull(userId, "userId cannot be null");
		return apiService.getDocumentCount(senderId, agreementType, userId, query, requestTrackingId, singleJaxbEntityHandler(DocumentCount.class)).getCount();
	}

	public DocumentContent getDocumentContent(final SenderId senderId, final AgreementType agreementType, final long documentId) {
		return getDocumentContent(senderId, agreementType, documentId, null);
	}

	public DocumentContent getDocumentContent(final SenderId senderId, final AgreementType agreementType, final long documentId, final String requestTrackingId) {
		return apiService.getDocumentContent(senderId, agreementType, documentId, requestTrackingId, singleJaxbEntityHandler(DocumentContent.class));
	}

	public Stream<UserId> getAgreementUsers(final SenderId senderId, final AgreementType agreementType, final Boolean smsNotificationEnabled) {
		return getAgreementUsers(senderId, agreementType, smsNotificationEnabled, null);
	}

	public Stream<UserId> getAgreementUsers(final SenderId senderId, final AgreementType agreementType, final Boolean smsNotificationEnabled, final String requestTrackingId) {
		Objects.requireNonNull(senderId, "senderId cannot be null");
		Objects.requireNonNull(agreementType, "agreementType cannot be null");
		return apiService.getAgreementUsers(senderId, agreementType, smsNotificationEnabled, requestTrackingId);
	}

	private ResponseHandler<Void> voidOkHandler() {
		return response -> mapOkResponseOrThrowException(response, r -> null);
	}

	private <T> ResponseHandler<T> singleJaxbEntityHandler(Class<T> responseType) {
		return response -> mapOkResponseOrThrowException(response, r -> unmarshallEntity(r, responseType));
	}

	public static class Builder {

		private static final URI PRODUCTION_ENDPOINT = URI.create("https://api.digipost.no");

		private URI serviceEndpoint;
		private final BrokerId brokerId;
		private final InputStream certificateP12File;
		private final String certificatePassword;
		private final Optional<PrivateKey> privateKey;
		private HttpClientBuilder httpClientBuilder;
		private Optional<HttpHost> proxyHost = Optional.empty();

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
			this.privateKey = Optional.ofNullable(privateKey);
			serviceEndpoint(PRODUCTION_ENDPOINT);
			httpClientBuilder = DigipostHttpClientFactory.createBuilder(DigipostHttpClientSettings.DEFAULT);
		}

		public Builder useProxy(final HttpHost proxyHost) {
			this.proxyHost = Optional.ofNullable(proxyHost);
			return this;
		}

		public Builder serviceEndpoint(URI endpointUri) {
			this.serviceEndpoint = endpointUri;
			return this;
		}

        public Builder setHttpClientBuilder(final HttpClientBuilder httpClientBuilder) {
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
				SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContextBuilder.build(), (hostname, session) -> true);
				httpClientBuilder.setSSLSocketFactory(sslConnectionSocketFactory);
			} catch (Exception e) {
				throw new RuntimeException("Could not disable certificate verification: " + e.getMessage(), e);
			}
			System.err.println("Not checking validity of certificates for any hostnames");
			return this;
		}

		public DigipostUserAgreementsClient build() {
			CryptoUtil.addBouncyCastleProviderAndVerify_AES256_CBC_Support();

			httpClientBuilder.addInterceptorLast(new RequestDateInterceptor());
			httpClientBuilder.addInterceptorLast(new RequestUserAgentInterceptor());
			PrivateKeySigner pkSigner = privateKey.map(PrivateKeySigner::new).orElseGet(() -> new PrivateKeySigner(certificateP12File, certificatePassword));
			httpClientBuilder.addInterceptorLast(new RequestSignatureInterceptor(pkSigner, new RequestContentSHA256Filter()));
			httpClientBuilder.addInterceptorLast(new ResponseDateInterceptor());
			proxyHost.ifPresent(httpClientBuilder::setProxy);

			ApiService apiService = new ApiService(serviceEndpoint, brokerId, httpClientBuilder.build());
			return new DigipostUserAgreementsClient(apiService);
		}
	}
}
