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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

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
	private String subject;
	@XmlElement(name = "delivery-time")
	private ZonedDateTime deliveryTime;
	@XmlElement(name = "read")
	private Boolean read;
	@XmlElement(name = "authentication-level")
	private String authenticationLevel;
	@XmlElement(name = "digipost-uri")
	private String digipostUri;

	private Document() {}

	public Document(final long id) {
		this.id = id;
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

	public boolean isRead() {
		return read != null && read;
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
		sb.append(", authenticationLevel=").append(authenticationLevel);
		sb.append(", deliveryTime=").append(deliveryTime);
		sb.append('}');
		return sb.toString();
	}
}
