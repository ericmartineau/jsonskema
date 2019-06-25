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
import assertk.assertThat
import io.mverse.jsonschema.JsonSchemas
import io.mverse.jsonschema.assertj.asserts.hasErrorCode
import io.mverse.jsonschema.assertj.asserts.hasViolationAt
import io.mverse.jsonschema.assertj.asserts.validating
import io.mverse.jsonschema.createSchemaReader
import io.mverse.jsonschema.resourceLoader
import org.junit.Test

class InvalidObjectInArrayTest {

  @Test
  fun test() {

    val schemaJson = JsonSchemas.resourceLoader().readJsonObject("/io/mverse/jsonschema/invalidobjectinarray/schema.json")
    val valueJson = JsonSchemas.resourceLoader().readJsonObject("/io/mverse/jsonschema/invalidobjectinarray/subject.json")
    val schema = JsonSchemas.createSchemaReader().readSchema(schemaJson)

    assertThat(schema)
        .validating(valueJson)
        .hasViolationAt("#/notification/target/apps/0")
        .hasErrorCode("validation.keyword.required")
  }
}
