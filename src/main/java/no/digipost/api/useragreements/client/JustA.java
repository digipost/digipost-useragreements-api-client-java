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
 /*
 * An implementation of the JustA-pattern from github.com/digipost/digg,
 * with added support for validation, that will also work with JDK7
 *
 */
package no.digipost.api.useragreements.client;

import java.util.Objects;
import java.util.function.Function;

public abstract class JustA<T> {

    protected final T value;
	private final Function<? super T, String> valueSerializer;

    protected JustA(T value, Function<? super T, String> valueSerializer) {
        this.value = value;
		this.valueSerializer = valueSerializer;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof JustA && getClass().isInstance(obj)) {
            JustA<?> that = (JustA<?>) obj;
            return Objects.equals(this.value, that.value);
        }
        return false;
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(value);
    }

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{" +
				"value=" + value +
				'}';
	}

	public final String serialize() {
		return valueSerializer.apply(value);
	}
}
