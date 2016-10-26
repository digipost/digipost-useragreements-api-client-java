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


import no.digipost.api.useragreements.client.xml.ErrorCodeXmlAdapter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "error")
public class Error {

	@XmlElement(name = "error-code", required = true)
	@XmlJavaTypeAdapter(ErrorCodeXmlAdapter.class)
	private final ErrorCode code;

	@XmlElement(name = "error-message", required = true)
	private final String message;

	private Error(final ErrorCode code, final String message) {
		this.code = code;
		this.message = message;
	}

	@Override
	public String toString() {
		return "Error{" +
				"code=" + code +
				", message='" + message + '\'' +
				'}';
	}

	public ErrorCode getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public boolean hasCode(final ErrorCode errorCode) {
		return code == errorCode;
	}
}
