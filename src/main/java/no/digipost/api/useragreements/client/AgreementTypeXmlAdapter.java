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

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class AgreementTypeXmlAdapter extends XmlAdapter<String,AgreementType> {
    @Override
    public AgreementType unmarshal(String v) {
        for (AgreementType t : AgreementType.values()) {
            if ( t.getType().compareTo(v) == 0 )
                return t;
        }
        throw new IllegalArgumentException("Value " + v + " is illegal for " + AgreementType.class);
    }

    @Override
    public String marshal(AgreementType v) {
        return v.getType();
    }
}
