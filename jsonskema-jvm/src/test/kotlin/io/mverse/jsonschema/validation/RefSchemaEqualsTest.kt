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
import assertk.assertions.hasToString
import io.mverse.jsonschema.JsonSchemas
import io.mverse.jsonschema.assertj.asserts.isEqualIgnoringWhitespace
import io.mverse.jsonschema.resourceLoader
import io.mverse.jsonschema.createSchemaReader
import io.mverse.jsonschema.impl.RefJsonSchema
import io.mverse.jsonschema.loading.parseJsrObject
import io.mverse.jsonschema.schema
import lang.json.JsonKey
import lang.json.getOrNull
import lang.net.URI
import nl.jqno.equalsverifier.EqualsVerifier
import nl.jqno.equalsverifier.Warning
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class RefSchemaEqualsTest {

  @Test
  fun toStringTest() {
    val rawSchemaJson = JsonSchemas.resourceLoader().readJsonObject("tostring/ref.json")
    val schema = JsonSchemas.createSchemaReader().readSchema(rawSchemaJson)
    val actual = schema.toString()

    assertThat(actual.parseJsrObject().getOrNull(JsonKey("properties"))).isEqualTo(rawSchemaJson["properties"])
  }

  @Test
  fun toStringTest_Builder() {
    val schema = schema {
      properties["foo"] = {
        ref = URI("#")
      }
    }
    assert(schema.toString()).isEqualIgnoringWhitespace("{\"properties\":{\"foo\":{\"\$ref\":\"#\"}}}")
  }

  @Test
  fun equalsTest() {
    EqualsVerifier.forClass(RefJsonSchema::class.java)
        .withOnlyTheseFields("refURI")
        .suppress(Warning.STRICT_INHERITANCE)
        .verify()
  }
}
