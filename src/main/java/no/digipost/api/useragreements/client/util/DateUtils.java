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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

public class DateUtils {

	public static final ZoneId GMT = ZoneId.of("GMT");

	/**
	 * Format a {@code ZonedDateTime} as RFC 1123 date format used in HTTP.
	 * The format is {@link DateTimeFormatter#RFC_1123_DATE_TIME}.
	 *
	 * @param dateTime The {@code ZonedDateTime} to format.
	 * @return the dateTime formatted as RFC 1123 compliant string
	 */
	public static String formatDate(ZonedDateTime dateTime) {
		return RFC_1123_DATE_TIME.format(dateTime.withZoneSameInstant(GMT));
	}

	/**
	 * Return an {@code ZonedDateTime} parsed from an RFC 1123 compliant string, i.e.
	 * having the format {@link DateTimeFormatter#RFC_1123_DATE_TIME}.
	 *
	 * @param dateTime the RFC 1123 compliant string to parse.
	 * @return the parsed {@code ZonedDateTime}
	 */
	public static ZonedDateTime parseDate(String dateTime) {
		return RFC_1123_DATE_TIME.parse(dateTime, ZonedDateTime::from);
	}

}
