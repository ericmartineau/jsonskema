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

import io.mverse.jsonschema.JsonSchemas
import io.mverse.jsonschema.assertj.asserts.hasKeyword
import io.mverse.jsonschema.assertj.asserts.hasViolationCount
import io.mverse.jsonschema.assertj.asserts.isNotValid
import io.mverse.jsonschema.assertj.asserts.validating
import io.mverse.jsonschema.keyword.Keywords.ALL_OF
import io.mverse.jsonschema.keyword.Keywords.ANY_OF
import io.mverse.jsonschema.keyword.Keywords.ONE_OF
import io.mverse.jsonschema.loading.parseJsrJson
import io.mverse.jsonschema.schema
import io.mverse.jsonschema.validation.ValidationMocks.mockNumberSchema
import lang.json.toJsrValue
import org.junit.Test
import java.util.Arrays.asList

class CombinedKeywordValidatorTest {

  @Test
  fun reportCauses() {
    val parentSchema = JsonSchemas.schema {
      allOfSchemas = SUBSCHEMAS
    }
    val subject = "24".parseJsrJson()
    parentSchema.validating(subject)
        .isNotValid()
        .hasViolationCount(1)
        .hasKeyword(ALL_OF)
  }

  @Test
  fun validateAll() {
    JsonSchemas.schema { allOfSchemas = SUBSCHEMAS }
        .validating(20.toJsrValue())
        .hasKeyword(ALL_OF)
  }

  @Test
  fun validateAny() {
    JsonSchemas.schema { anyOfSchemas = SUBSCHEMAS }
        .validating(5.toJsrValue())
        .hasKeyword(ANY_OF)
  }

  @Test
  fun validateOne() {
    JsonSchemas.schema { oneOfSchemas = SUBSCHEMAS }
        .validating(30.toJsrValue())
        .hasKeyword(ONE_OF)
  }

  companion object {
    private val SUBSCHEMAS = asList(
        mockNumberSchema.apply { multipleOf = 10 },
        mockNumberSchema.apply { multipleOf = 3 })
  }
}
