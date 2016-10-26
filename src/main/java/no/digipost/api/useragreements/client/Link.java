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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.net.URI;
import java.net.URISyntaxException;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "link", propOrder = { "rel", "uri" })
public class Link {
	@XmlAttribute
	private String rel;
	@XmlAttribute
	private String uri;

	public String getRel() {
		return rel.substring(rel.lastIndexOf("/") + 1);
	}

	public URI getUri() {
		try {
			return new URI(uri);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public boolean equalsRelation(final String relation) {
		return relation.equals(getRel());
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(31, 1).append(rel).append(uri).toHashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		Link other = (Link) obj;
		return new EqualsBuilder()
				.append(rel, other.rel)
				.append(uri, other.uri)
				.isEquals();
	}
}
