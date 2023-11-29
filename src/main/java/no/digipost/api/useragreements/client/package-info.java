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
@XmlSchema(namespace = "http://api.digipost.no/user/schema/v2", elementFormDefault = QUALIFIED)
@XmlJavaTypeAdapters({
		@XmlJavaTypeAdapter(type = Instant.class, value = InstantXmlAdapter.class),
		@XmlJavaTypeAdapter(type = LocalDate.class, value = DateXmlAdapter.class),
		@XmlJavaTypeAdapter(type = ZonedDateTime.class, value = ZonedDateTImeXmlAdapter.class),
		@XmlJavaTypeAdapter(type = UserId.class, value = UserIdXmlAdapter.class)
})
package no.digipost.api.useragreements.client;

import jakarta.xml.bind.annotation.XmlSchema;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapters;
import no.digipost.api.useragreements.client.xml.DateXmlAdapter;
import no.digipost.api.useragreements.client.xml.InstantXmlAdapter;
import no.digipost.api.useragreements.client.xml.UserIdXmlAdapter;
import no.digipost.api.useragreements.client.xml.ZonedDateTImeXmlAdapter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;

import static jakarta.xml.bind.annotation.XmlNsForm.QUALIFIED;
