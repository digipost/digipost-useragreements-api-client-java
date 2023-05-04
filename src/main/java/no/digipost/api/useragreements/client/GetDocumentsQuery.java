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


import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

public class GetDocumentsQuery {
	private final OffsetDateTime deliveryTimeFrom;
	private final OffsetDateTime deliveryTimeTo;

	private GetDocumentsQuery(final Builder builder) {
		this.deliveryTimeFrom = builder.deliveryTimeFrom;
		this.deliveryTimeTo = builder.deliveryTimeTo;
	}

	public OffsetDateTime getDeliveryTimeFrom() {
		return deliveryTimeFrom;
	}

	public OffsetDateTime getDeliveryTimeTo() {
		return deliveryTimeTo;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static GetDocumentsQuery empty() {
		return builder().build();
	}

	public static class Builder {
		private OffsetDateTime deliveryTimeFrom;
		private OffsetDateTime deliveryTimeTo;

		private Builder() {}

		public Builder deliveryTimeFrom(final OffsetDateTime deliveryTimeFrom) {
			this.deliveryTimeFrom = deliveryTimeFrom;
			return this;
		}

		public Builder deliveryTimeFrom(final ZonedDateTime deliveryTimeFrom) {
			return deliveryTimeFrom(deliveryTimeFrom.toOffsetDateTime());
		}

		public Builder deliveryTimeTo(final OffsetDateTime deliveryTimeTo) {
			this.deliveryTimeTo = deliveryTimeTo;
			return this;
		}

		public Builder deliveryTimeTo(final ZonedDateTime deliveryTimeTo) {
			return deliveryTimeTo(deliveryTimeTo.toOffsetDateTime());
		}

		public GetDocumentsQuery build() {
			return new GetDocumentsQuery(this);
		}
	}
}
