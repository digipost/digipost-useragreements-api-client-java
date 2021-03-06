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


import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

public class GetDocumentsQuery {
	private final InvoiceStatus invoiceStatus;
	private final LocalDate invoiceDueDateFrom;
	private final LocalDate invoiceDueDateTo;
	private final OffsetDateTime deliveryTimeFrom;
	private final OffsetDateTime deliveryTimeTo;

	private GetDocumentsQuery(final Builder builder) {
		this.invoiceStatus = builder.invoiceStatus;
		this.invoiceDueDateFrom = builder.invoiceDueDateFrom;
		this.invoiceDueDateTo = builder.invoiceDueDateTo;
		this.deliveryTimeFrom = builder.deliveryTimeFrom;
		this.deliveryTimeTo = builder.deliveryTimeTo;
	}

	public InvoiceStatus getInvoiceStatus() {
		return invoiceStatus;
	}

	public LocalDate getInvoiceDueDateFrom() {
		return invoiceDueDateFrom;
	}

	public LocalDate getInvoiceDueDateTo() {
		return invoiceDueDateTo;
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
		private InvoiceStatus invoiceStatus;
		private LocalDate invoiceDueDateFrom;
		private LocalDate invoiceDueDateTo;
		private OffsetDateTime deliveryTimeFrom;
		private OffsetDateTime deliveryTimeTo;

		private Builder() {}

		public Builder invoiceStatus(final InvoiceStatus invoiceStatus) {
			this.invoiceStatus = invoiceStatus;
			return this;
		}

		public Builder invoiceDueDateFrom(final LocalDate invoiceDueDateFrom) {
			this.invoiceDueDateFrom = invoiceDueDateFrom;
			return this;
		}

		public Builder invoiceDueDateTo(final LocalDate invoiceDueDateTo) {
			this.invoiceDueDateTo = invoiceDueDateTo;
			return this;
		}

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
