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

import io.mverse.jsonschema.formats.FormatChecks
import io.mverse.jsonschema.formats.TIME_FORMATS_ACCEPTED
import io.mverse.jsonschema.validation.FormatValidator
import lang.string.format

/**
 * Implementation of the "date-time" format value.
 */
class TimeFormatValidator : FormatValidator {

  override fun validate(subject: String): String? = try {
    FormatChecks.isValidIsoTime(subject)
    null
  } catch (e: Exception) {
    "[%s] is not a valid time. Expected %s".format(subject, TIME_FORMATS_ACCEPTED)
  }

  override fun formatName(): String {
    return "time"
  }
}
