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
import assertk.assertions.containsAll
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.SchemaBuilder
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.loading.parseJsonObject
import io.mverse.jsonschema.schemaBuilder
import io.mverse.jsonschema.validation.ValidationMocks.createTestValidator
import io.mverse.jsonschema.validation.ValidationTestSupport.expectSuccess
import lang.json.toJsonLiteral
import org.junit.Assert
import org.junit.Test

class EmptySchemaTest {

  @Test
  fun testAllGenericProps() {
    val actual = roundTripSchema("my title", "my description", "my/id")
    assert(actual.keys).containsAll("title", "description", "\$id")
  }

  @Test
  fun testBuilder() {
    Assert.assertEquals(JsonSchema.schemaBuilder().build(), JsonSchema.schemaBuilder().build())
  }

  @Test
  fun testOnlyId() {
    val actual = roundTripSchema(id = "my/id")
    assert(actual.keys).hasSize(1)
    assert(actual[Keywords.DOLLAR_ID_KEY].string).isEqualTo("my/id")
  }

  @Test
  fun testOnlySchemaDescription() {
    val actual = roundTripSchema(description = "descr")
    assert(actual.keys).hasSize(1)
    assert(actual["description"].string).isEqualTo("descr")
  }

  @Test
  fun testOnlyTitle() {
    val actual = roundTripSchema("my title", null, null)
    assert(actual.keys).containsAll("title")
    assert(actual["title"].string).isEqualTo("my title")
  }

  @Test
  fun testToString() {
    assert(JsonSchema.schemaBuilder().build().toString()).isEqualTo("{}")
  }

  @Test
  fun testValidate() {
    expectSuccess { ValidationMocks.createTestValidator(JsonSchema.schemaBuilder().build()).validate("something".toJsonLiteral()) }
  }

  private fun roundTripSchema(title: String? = null,
                              description: String? = null,
                              id: String? = null): kotlinx.serialization.json.JsonObject {
    val builder: SchemaBuilder = if (id?.isNotBlank() == true) {
      JsonSchema.schemaBuilder(id)
    } else {
      JsonSchema.schemaBuilder()
    }
    if (title != null) builder.title(title)
    if (description != null) builder.description(description)
    return builder.build().toString().parseJsonObject()
  }
}
