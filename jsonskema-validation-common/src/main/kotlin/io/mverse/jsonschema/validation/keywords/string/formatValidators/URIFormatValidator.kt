/*
 * Copyright (C) 2017 MVerse (http://mverse.io)
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
package io.mverse.jsonschema.validation.keywords.string.formatValidators

import io.mverse.jsonschema.validation.FormatValidator
import lang.net.URI
import lang.string.format

/**
 * Implementation of the "uri" format value.
 */
open class URIFormatValidator : FormatValidator {

  override fun validate(subject: String): String? = try {
    URI(subject)
    null
  } catch (e: Exception) {
    "[%s] is not a valid URI".format(subject)
  }

  override fun formatName(): String {
    return "uri"
  }
}
