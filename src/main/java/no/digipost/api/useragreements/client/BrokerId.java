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

public class BrokerId extends JustA<Long> {

	public static BrokerId of(long id) {
		return new BrokerId(id);
	}

	private BrokerId(final long id) {
		super(id, String::valueOf);
		if (id <= 0) {
			throw new IllegalArgumentException("broker id must be numeric > 0");
		}
	}

	public long getId() {
		return value;
	}

}
