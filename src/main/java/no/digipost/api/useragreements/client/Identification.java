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
import jakarta.xml.bind.annotation.XmlType;

import static no.digipost.api.useragreements.client.PersonalIdentificationNumber.mask;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "identification", propOrder = {
		"personalIdentificationNumber"
})
@XmlRootElement(name = "identification")
public final class Identification {

	@XmlElement(name = "personal-identification-number")
	private String personalIdentificationNumber;

	public Identification(String personalIdentificationNumber) {
		this.personalIdentificationNumber = personalIdentificationNumber;
	}

	//JAXB
	public Identification() {
	}

	public UserId toUserId() {
		return UserId.of(personalIdentificationNumber);
	}

	@Override
	public String toString() {
		return mask(personalIdentificationNumber);
	}
}

