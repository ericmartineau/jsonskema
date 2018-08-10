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
package io.mverse.jsonschema.integration

import assertk.assert
import assertk.assertions.isNull
import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.ValidationMocks
import io.mverse.jsonschema.resourceLoader
import io.mverse.jsonschema.schemaReader
import kotlinx.serialization.json.json
import kotlin.test.Test

class EmptyObjectTest {

  @Test
  fun validateEmptyObject() {
    val jsonSubject = json {
      "type" to "object"
      "properties" to json {}
    }
    val schemaJson = JsonSchema.resourceLoader().readJson("json-schema-draft-06.json")

    val schema = JsonSchema.schemaReader().readSchema(schemaJson.jsonObject)
    val validator = ValidationMocks.createTestValidator(schema)
    val errors = validator.validate(jsonSubject)
    assert(errors).isNull()
  }
}
