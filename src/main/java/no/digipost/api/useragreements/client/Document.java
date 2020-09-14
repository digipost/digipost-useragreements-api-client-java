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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.time.ZonedDateTime;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "user-document")
public class Document {

	@XmlElement
	private long id;
	@XmlElement(name = "sender-name")
	private String senderName;
	@XmlElement
	private Invoice invoice;
	@XmlElement
	private String subject;
	@XmlElement(name = "delivery-time")
	private ZonedDateTime deliveryTime;
	@XmlElement(name = "first-accessed")
	private ZonedDateTime firstAccessed;
	@XmlElement(name = "authentication-level")
	private String authenticationLevel;
	@XmlElement(name = "digipost-uri")
	private String digipostUri;

	private Document() {}

	public Document(final long id, final Invoice invoice) {
		this.id = id;
		this.invoice = invoice;
	}

	public Invoice getInvoice() {
		return invoice;
	}

	public String getSenderName() {
		return senderName;
	}

	public long getId() {
		return id;
	}

	public String getSubject() {
		return subject;
	}

	public ZonedDateTime getDeliveryTime() {
		return deliveryTime;
	}

	public ZonedDateTime getFirstAccessed() {
		return firstAccessed;
	}

	public boolean isRead() {
		return getFirstAccessed() != null;
	}

	public AuthenticationLevel getAuthenticationLevel() {
		return AuthenticationLevel.valueOf(authenticationLevel);
	}

	public URI getDigipostUri() {
		return URI.create(digipostUri);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("Document{");
		sb.append("id=").append(id);
		sb.append(", senderName='").append(senderName).append('\'');
		sb.append(", invoice=").append(invoice);
		sb.append(", authenticationLevel=").append(authenticationLevel);
		sb.append(", deliveryTime=").append(deliveryTime);
		sb.append('}');
		return sb.toString();
	}
}
