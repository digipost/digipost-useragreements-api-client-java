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

import java.util.Arrays;

public enum AuthenticationLevel {
	IDPORTEN_4("Id-Porten Two-Factor"),
	IDPORTEN_3("Id-Porten MinId", IDPORTEN_4),
	TWO_FACTOR("Two-Factor", IDPORTEN_4),
	PASSWORD("Password", TWO_FACTOR, IDPORTEN_3, IDPORTEN_4);

	final String description;
	final AuthenticationLevel[] acceptable;

	AuthenticationLevel(String description, AuthenticationLevel... acceptable) {
		this.description = description;
		this.acceptable = acceptable;
	}

	boolean isAccessibleAtLevel(AuthenticationLevel other) {
		return Arrays.asList(this, acceptable).contains(other);
	}
}
