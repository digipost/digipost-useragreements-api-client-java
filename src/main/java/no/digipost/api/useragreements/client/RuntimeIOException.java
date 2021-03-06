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

public class RuntimeIOException extends UserAgreementsApiException {

	private static final long serialVersionUID = 1;

	public static RuntimeIOException from(Throwable cause) {
		if (cause instanceof RuntimeIOException) {
			return (RuntimeIOException) cause;
		} else {
			return new RuntimeIOException(cause != null ? cause.getClass().getSimpleName() + ": '" + cause.getMessage() + "'" : null, cause);
		}
	}

	public RuntimeIOException(final String message) {
		this(message, null);
	}

	public RuntimeIOException(final String message, final Throwable cause) {
		super(ErrorCode.IO_EXCEPTION, message, cause);
	}

}
