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

import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Agreements {

	@XmlElement(name = "agreement")
	private List<Agreement> agreements;

	private Agreements() {
		this(new ArrayList<Agreement>());
	}

	public Agreements(List<Agreement> agreements) {
		this.agreements = agreements;
	}

	public List<Agreement> getAgreements() {
		return agreements;
	}

	/**
	 * Convenience method with fail-fast to retrieve the {@link Agreement} from an
	 * {@link Agreements} instance expected to contain exactly one Agreement.
	 *
	 * @return the single agreement expected to be contained in this {@code Agreements} instance.
	 * @throws IllegalStateException if this object contains none or more than one {@code Agreement}.
	 */
	public Agreement getSingleAgreement() {
		if (agreements.isEmpty()) {
			throw new IllegalStateException("Contained 0 agreements, but expected to be exactly one.");
		} else if (agreements.size() > 1) {
			throw new IllegalStateException("Expected exactly one, but contained " + agreements.size() + " agreements: " + agreements);
		} else {
			return agreements.get(0);
		}
	}
}
