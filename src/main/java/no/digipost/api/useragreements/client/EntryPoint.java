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
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "entrypoint")
public class EntryPoint {

	@XmlElement(name = "certificate", required = false)
	private String certificate;

	@XmlElement(name = "link")
	protected List<Link> link;

	protected Link getLinkByRelationName(final String relation) {
		for (Link l : link) {
			if (l.equalsRelation(relation)) {
				return l;
			}
		}
		return null;
	}

	public String getCertificate() {
		return certificate;
	}

	public URI getIdentificationUri() {
		return getLinkByRelationName("identify_recipient").getUri();
	}
}
