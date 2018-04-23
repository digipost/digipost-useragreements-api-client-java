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

public final class Headers {

	private static final String X_Digipost_Prefix = "X-Digipost-";

	/**
	 * X-Digipost-Signature (Digipost-specific header)
	 */
	public static final String X_Digipost_Signature = X_Digipost_Prefix + "Signature";

	/**
	 * X-Digipost-UserId (Digipost-specific header)
	 */
	public static final String X_Digipost_UserId = X_Digipost_Prefix + "UserId";

	/**
	 * Content-MD5
	 *
	 * @see <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.15">RFC 2616 section 14.15</a>
	 */
	public static final String Content_MD5 = "Content-MD5";

	/**
	 * X-Content-SHA256 (Digipost-specific header)
	 */
	public static final String X_Content_SHA256 = "X-Content-SHA256";

}
