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
package no.digipost.api.useragreements.client.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.ZonedDateTime;

import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;

public class ZonedDateTImeXmlAdapter extends XmlAdapter<String, ZonedDateTime> {

	@Override
	public ZonedDateTime unmarshal(String value) {
		if (value == null) {
			return null;
		}
		return ZonedDateTime.from(ISO_ZONED_DATE_TIME.parse(value));

	}

	@Override
	public String marshal(ZonedDateTime dateTime) {
		if (dateTime == null) {
			return null;
		}
		return ISO_ZONED_DATE_TIME.format(dateTime);
	}
}
