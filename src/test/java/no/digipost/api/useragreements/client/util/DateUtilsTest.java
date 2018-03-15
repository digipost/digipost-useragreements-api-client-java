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
package no.digipost.api.useragreements.client.util;

import io.vavr.test.Arbitrary;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import static io.vavr.test.Property.def;
import static no.digipost.api.useragreements.client.util.DateUtils.formatDate;
import static no.digipost.api.useragreements.client.util.DateUtils.parseDate;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DateUtilsTest {

	@Test
	public void formatsAsRFC_1123() {
		DateTime flightToJapan = new DateTime(2018, 3, 26, 13, 30, DateTimeZone.forID("Europe/Oslo"));
		String rfc1123Formatted = DateUtils.formatDate(flightToJapan);
		assertThat(rfc1123Formatted, is("Mon, 26 Mar 2018 11:30:00 GMT"));
	}

	@Test
	public void convertsBackAndForth() {
		def("formats as RFC 1123 and converts back to datetime instance")
			.forAll(Arbitrary.integer().map(epochMillis -> new DateTime((long) epochMillis).withMillisOfSecond(0)))
			.suchThat(dateTime -> dateTime.isEqual(parseDate(formatDate(dateTime))))
			.check()
			.assertIsSatisfied();
	}
}
