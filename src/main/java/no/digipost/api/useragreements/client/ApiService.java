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

import no.digipost.cache2.inmemory.SingleCached;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import javax.xml.bind.JAXB;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.Stream;

import static java.time.Duration.ofMinutes;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static no.digipost.api.useragreements.client.Headers.X_Digipost_UserId;
import static no.digipost.api.useragreements.client.util.ResponseUtils.mapOkResponseOrThrowException;
import static no.digipost.api.useragreements.client.util.ResponseUtils.unmarshallEntities;
import static no.digipost.api.useragreements.client.util.ResponseUtils.unmarshallEntity;
import static no.digipost.cache2.inmemory.CacheConfig.expireAfterAccess;
import static no.digipost.cache2.inmemory.CacheConfig.useSoftValues;

public class ApiService {

	public static final String DIGIPOST_MEDIA_TYPE_USERS_V1 = "application/vnd.digipost.user-v1+xml";
	private static final String USER_DOCUMENTS_PATH = "user-documents";
	private static final String USER_AGREEMENTS_PATH = "user-agreements";

	private final URI serviceEndpoint;
	private final BrokerId brokerId;
	private final CloseableHttpClient httpClient;

	public ApiService(final URI serviceEndpoint, final BrokerId brokerId, final CloseableHttpClient httpClient) {
		this.serviceEndpoint = serviceEndpoint;
		this.brokerId = brokerId;
		this.httpClient = httpClient;
	}

	public IdentificationResult identifyUser(final SenderId senderId, final UserId userId, final String requestTrackingId, final ResponseHandler<IdentificationResult> handler) {
		return executeHttpRequest(newPostRequest(getEntryPoint().getIdentificationUri(), requestTrackingId, new Identification(userId.serialize())), handler);
	}

	public void createAgreement(final SenderId senderId, final Agreement agreement, final String requestTrackingId, final ResponseHandler<Void> handler) {
		executeHttpRequest(newPostRequest(new URIBuilder(serviceEndpoint).setPath(userAgreementsPath(senderId)), requestTrackingId, agreement), handler);
	}

	public GetAgreementResult getAgreement(final SenderId senderId, final AgreementType agreementType, final UserId userId, final String requestTrackingId, final ResponseHandler<GetAgreementResult> handler) {
		URIBuilder uriBuilder = new URIBuilder(serviceEndpoint)
				.setPath(userAgreementsPath(senderId))
				.setParameter("user-id", userId.serialize())
				.setParameter("agreement-type", agreementType.getType());
		return executeHttpRequest(newGetRequest(uriBuilder, requestTrackingId), handler);
	}

	public Agreements getAgreements(final SenderId senderId, final UserId userId, final String requestTrackingId, final ResponseHandler<Agreements> handler) {
		URIBuilder uriBuilder = new URIBuilder(serviceEndpoint)
				.setPath(userAgreementsPath(senderId))
				.setParameter("user-id", userId.serialize());
		return executeHttpRequest(newGetRequest(uriBuilder, requestTrackingId), handler);
	}

	public void deleteAgrement(final SenderId senderId, final AgreementType agreementType, final UserId userId, final String requestTrackingId, final ResponseHandler<Void> handler) {
		URIBuilder uriBuilder = new URIBuilder(serviceEndpoint)
				.setPath(userAgreementsPath(senderId))
				.setParameter("user-id", userId.serialize())
				.setParameter("agreement-type", agreementType.getType());
		HttpDelete deleteAgreementRequest = new HttpDelete(buildUri(uriBuilder));
		executeHttpRequest(withRequestTrackingHeader(deleteAgreementRequest, requestTrackingId), handler);
	}

	public Documents getDocuments(final SenderId senderId, final AgreementType agreementType, final UserId userId, final GetDocumentsQuery query, final String requestTrackingId, final ResponseHandler<Documents> handler) {
		URIBuilder uriBuilder = new URIBuilder(serviceEndpoint)
				.setPath(userDocumentsPath(senderId))
				.setParameter(UserId.QUERY_PARAM_NAME, userId.serialize())
				.setParameter(AgreementType.QUERY_PARAM_NAME, agreementType.getType());
		setGetDocumentsQueryParams(uriBuilder, query);
		return executeHttpRequest(newGetRequest(uriBuilder, requestTrackingId), handler);
	}

	private void setGetDocumentsQueryParams(final URIBuilder uriBuilder, final GetDocumentsQuery query) {
		if (query.getInvoiceStatus() != null) {
			uriBuilder.setParameter(InvoiceStatus.QUERY_PARAM_NAME, query.getInvoiceStatus().getStatus());
		}
		if (query.getInvoiceDueDateFrom() != null) {
			uriBuilder.setParameter("invoice-due-date-from", query.getInvoiceDueDateFrom().format(ISO_LOCAL_DATE));
		}
		if (query.getInvoiceDueDateTo() != null) {
			uriBuilder.setParameter("invoice-due-date-to", query.getInvoiceDueDateTo().format(ISO_LOCAL_DATE));
		}
		if (query.getDeliveryTimeFrom() != null) {
			uriBuilder.setParameter("delivery-time-from", query.getDeliveryTimeFrom().format(ISO_DATE_TIME));
		}
		if (query.getDeliveryTimeTo() != null) {
			uriBuilder.setParameter("delivery-time-to", query.getDeliveryTimeTo().format(ISO_DATE_TIME));
		}
	}

	public Document getDocument(final SenderId senderId, final AgreementType agreementType, final long documentId, final String requestTrackingId, final ResponseHandler<Document> handler) {
		URIBuilder uriBuilder = new URIBuilder(serviceEndpoint)
				.setPath(userDocumentsPath(senderId) + "/" + documentId)
				.setParameter(AgreementType.QUERY_PARAM_NAME, agreementType.getType());
		return executeHttpRequest(newGetRequest(uriBuilder, requestTrackingId), handler);
	}

	public void updateInvoice(final SenderId senderId, final AgreementType agreementType, final long documentId, final InvoiceUpdate invoice, final String requestTrackingId, final ResponseHandler<Void> handler) {
		URIBuilder uriBuilder = new URIBuilder(serviceEndpoint)
				.setPath(userDocumentsPath(senderId) + "/" + documentId + "/invoice")
				.setParameter(AgreementType.QUERY_PARAM_NAME, agreementType.getType());
		executeHttpRequest(newPostRequest(uriBuilder, requestTrackingId, invoice), handler);
	}

	public DocumentCount getDocumentCount(final SenderId senderId, final AgreementType agreementType, final UserId userId, final GetDocumentsQuery query, final String requestTrackingId, final ResponseHandler<DocumentCount> handler) {
		URIBuilder uriBuilder = new URIBuilder(serviceEndpoint)
				.setPath(userDocumentsPath(senderId) + "/count")
				.setParameter(UserId.QUERY_PARAM_NAME, userId.serialize())
				.setParameter(AgreementType.QUERY_PARAM_NAME, agreementType.getType());
		setGetDocumentsQueryParams(uriBuilder, query);
		return executeHttpRequest(newGetRequest(uriBuilder, requestTrackingId), handler);
	}

	public DocumentContent getDocumentContent(final SenderId senderId, final AgreementType agreementType, final long documentId, final String requestTrackingId, final ResponseHandler<DocumentContent> handler) {
		URIBuilder uriBuilder = new URIBuilder(serviceEndpoint)
				.setPath(userDocumentsPath(senderId) + "/" + documentId + "/content")
				.setParameter(AgreementType.QUERY_PARAM_NAME, agreementType.getType());
		return executeHttpRequest(newGetRequest(uriBuilder, requestTrackingId), handler);
	}

	public Stream<UserId> getAgreementUsers(final SenderId senderId, final AgreementType agreementType, final Boolean smsNotificationsEnabled, final String requestTrackingId) {
		URIBuilder uriBuilder = new URIBuilder(serviceEndpoint)
				.setPath(userAgreementsPath(senderId) + "/agreement-users")
				.setParameter(AgreementType.QUERY_PARAM_NAME, agreementType.getType());
		if (smsNotificationsEnabled != null) {
			uriBuilder
				.setParameter("invoice-sms-notification", smsNotificationsEnabled.toString());
		}

		HttpGet request = newGetRequest(uriBuilder, requestTrackingId);
		request.setHeader(X_Digipost_UserId, brokerId.serialize());
		CloseableHttpResponse response;
		try {
			response = httpClient.execute(request);
			return mapOkResponseOrThrowException(response, r -> unmarshallEntities(r, AgreementUsers.class).flatMap(a -> a.getUsers().stream()));
		} catch (IOException e) {
			throw new RuntimeIOException(e.getMessage(), e);
		}
	}


	private static String userAgreementsPath(final SenderId senderId) {
		return senderId.serialize() + "/" + USER_AGREEMENTS_PATH;
	}

	private static String userDocumentsPath(final SenderId senderId) {
		return senderId.serialize() + "/" + USER_DOCUMENTS_PATH;
	}

	private <T> T executeHttpRequest(final HttpRequestBase request, final ResponseHandler<T> handler) {
		try {
			request.setHeader(X_Digipost_UserId, brokerId.serialize());
			return httpClient.execute(request, handler);
		} catch (IOException e) {
			throw RuntimeIOException.from(e);
		}
	}

	private HttpGet newGetRequest(final URIBuilder uriBuilder, String requestTrackingId) {
		return newGetRequest(buildUri(uriBuilder), requestTrackingId);
	}

	private HttpGet newGetRequest(final URI uri, String requestTrackingId) {
		return withCommonHeaders(new HttpGet(uri), requestTrackingId);
	}

	private HttpPost newPostRequest(final URIBuilder uriBuilder, String requestTrackingId, Object postBodyEntity) {
		return newPostRequest(buildUri(uriBuilder), requestTrackingId, postBodyEntity);
	}

	private HttpPost newPostRequest(final URI uri, String requestTrackingId, Object postBodyEntity) {
		HttpPost request = withCommonHeaders(new HttpPost(uri), requestTrackingId);
		request.setHeader(HttpHeaders.CONTENT_TYPE, DIGIPOST_MEDIA_TYPE_USERS_V1);
		request.setEntity(marshallJaxbEntity(postBodyEntity));
		return request;
	}

	private static URI buildUri(URIBuilder builder) {
		try {
			return builder.build();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private static <REQ extends HttpRequestBase> REQ withCommonHeaders(REQ request, String requestTrackingId) {
		request.setHeader(HttpHeaders.ACCEPT, DIGIPOST_MEDIA_TYPE_USERS_V1);
		return withRequestTrackingHeader(request, requestTrackingId);
	}

	private static <REQ extends HttpRequestBase> REQ withRequestTrackingHeader(REQ request, final String requestTrackingId) {
		if (requestTrackingId != null && !requestTrackingId.isEmpty()) {
			request.setHeader("X-Digipost-Request-Id", requestTrackingId);
		}
		return request;
	}

	private static HttpEntity marshallJaxbEntity(final Object obj) {
		ByteArrayOutputStream bao = new ByteArrayOutputStream(1024);
		JAXB.marshal(obj, bao);
		return new ByteArrayEntity(bao.toByteArray());
	}

	public EntryPoint getEntryPoint() {
		try {
			return cachedEntryPoint.get();
		} catch (RuntimeException e) {
			if (e.getCause() instanceof UserAgreementsApiException) {
				throw (UserAgreementsApiException) e.getCause();
			} else {
				throw e;
			}
		}
	}


	private EntryPoint performGetEntryPoint() {
		return executeHttpRequest(newGetRequest(serviceEndpoint, null),
				response -> mapOkResponseOrThrowException(response, r -> unmarshallEntity(r, EntryPoint.class)));
	}


	private final SingleCached<EntryPoint> cachedEntryPoint =
			new SingleCached<>("digipost-entrypoint", this::performGetEntryPoint, expireAfterAccess(ofMinutes(5)), useSoftValues);
}
