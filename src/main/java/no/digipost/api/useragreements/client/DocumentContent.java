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
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import no.digipost.api.useragreements.client.xml.URIXmlAdapter;

import java.net.URI;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DocumentContent {

	@XmlElement(name = "content-type")
	private String contentType;

	@XmlElement(name = "uri")
	@XmlJavaTypeAdapter(URIXmlAdapter.class)
	private URI uri;

	private DocumentContent() {}

	public DocumentContent(final String contentType, final URI uri) {
		this.contentType = contentType;
		this.uri = uri;
	}

	public String getContentType() {
		return contentType;
	}

	public URI getTempUri() {
		return uri;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("DocumentContent{");
		sb.append("contentType='").append(contentType).append('\'');
		sb.append(", uri=").append(uri);
		sb.append('}');
		return sb.toString();
	}
}
