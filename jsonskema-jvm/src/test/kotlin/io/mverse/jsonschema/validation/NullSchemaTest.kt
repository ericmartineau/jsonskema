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

import assertk.assert
import assertk.assertions.isEqualTo
import io.mverse.jsonschema.assertj.asserts.isEqualIgnoringWhitespace
import io.mverse.jsonschema.assertj.asserts.isValid
import io.mverse.jsonschema.assertj.asserts.validating
import io.mverse.jsonschema.validation.ValidationMocks.mockNullSchema
import kotlinx.serialization.json.JsonNull
import lang.json.JsrNull
import org.junit.Test

class NullSchemaTest {

  @Test
  fun failure() {
    ValidationTestSupport.failureOf(mockNullSchema)
        .expectedKeyword("type")
        .input("null")
        .expect()
  }

  @Test
  fun success() {
    val obj = JsrNull
    mockNullSchema.build()
        .validating(obj)
        .isValid()
  }

  @Test
  fun toStringTest() {
    assert(mockNullSchema.build().toString()).isEqualIgnoringWhitespace("{\"type\":\"null\"}")
  }
}
