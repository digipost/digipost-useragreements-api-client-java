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

import java.util.function.Function;

/**
 * A user id string. May be e.g. a personal identification number (Norwegian national ID number),
 * or a bank account number.
 */
public class UserId extends JustA<String> {

	public static final String QUERY_PARAM_NAME = "user-id";

	public static UserId of(String userIdString) {
		return new UserId(userIdString);
	}

	private UserId(String userIdString) {
		super(userIdString, Function.identity());
	}

}
