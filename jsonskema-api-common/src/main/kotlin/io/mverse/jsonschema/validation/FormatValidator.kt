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
package io.mverse.jsonschema.validation


/**
 * Implementations perform the validation against the "format" keyword (see JSON Schema spec section
 * 7).
 */
interface FormatValidator {

  /**
   * Provides the name of this format.
   *
   *
   * Unless specified otherwise we use name to recognize string schemas using this format.
   *
   *
   * The default implementation of this method returns `"unnamed-format"`. It is strongly
   * recommended for implementations to give a more meaningful name by overriding this method.
   *
   * @return the format name.
   */
  fun formatName(): String {
    return "unnamed-format"
  }

  /**
   * Implementation-specific validation of `subject`. If a validation error occurs then
   * implementations should return a programmer-friendly error message as a String wrapped in an
   * Optional. If the validation succeeded then [an empty optional][Optional.empty] should be
   * returned.
   *
   * @param subject the string to be validated
   * @return an `Optional` wrapping the error message if a validation error occured, otherwise
   * [an empty optional][Optional.empty].
   */
  fun validate(subject: String): String?
}
