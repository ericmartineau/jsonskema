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

import io.mverse.jsonschema.assertj.asserts.hasKeyword
import io.mverse.jsonschema.assertj.asserts.isNotValid
import io.mverse.jsonschema.assertj.asserts.validating
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.validation.ValidationMocks.mockBooleanSchema
import io.mverse.jsonschema.validation.ValidationTestSupport.expectSuccess
import io.mverse.jsonschema.validation.ValidationTestSupport.failureOf
import lang.json.toJsrValue

import org.junit.Assert
import org.junit.Test

class BooleanSchemaTest {

  @Test
  fun whenStringFalse_SchemaFailsValidation() {
    mockBooleanSchema.build()
        .validating("false".toJsrValue())
        .isNotValid()
        .hasKeyword(Keywords.TYPE)
  }

  @Test
  fun success() {
    expectSuccess(mockBooleanSchema.build(), true)
  }

  @Test
  fun toStringTest() {
    Assert.assertEquals("{\"type\":\"boolean\"}", mockBooleanSchema.build().toString())
  }
}
