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

import no.digipost.api.useragreements.client.xml.AgreementTypeXmlAdapter;
import no.digipost.api.useragreements.client.xml.AttributesMapAdapter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.util.HashMap;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Agreement {

	@XmlElement(required = true)
	@XmlJavaTypeAdapter(AgreementTypeXmlAdapter.class)
	private AgreementType type;

	@XmlElement(name = "user-id", required = true)
	private String userId;

	@XmlElement
	@XmlJavaTypeAdapter(AttributesMapAdapter.class)
	private HashMap<String, String> attributes;

	public Agreement() {}

	public Agreement(final AgreementType type, final UserId userId, final HashMap<String, String> attributes) {
		this.type = type;
		this.userId = userId.serialize();
		this.attributes = attributes == null ? new HashMap<String, String>() : attributes;
	}

	public AgreementType getType() {
		return type;
	}

	public UserId getUserId() {
		return UserId.of(userId);
	}

	public HashMap<String, String> getAttributes() {
		return attributes;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("Agreement{");
		sb.append(", type=").append(type);
		sb.append(", userId='").append(userId).append('\'');
		sb.append(", attributes=").append(attributes);
		sb.append('}');
		return sb.toString();
	}
}
