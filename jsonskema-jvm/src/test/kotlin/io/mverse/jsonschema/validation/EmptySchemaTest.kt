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
import assertk.assertThat
import assertk.assertions.containsAll
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import io.mverse.jsonschema.JsonSchemas
import io.mverse.jsonschema.assertj.asserts.isEqualIgnoringWhitespace
import io.mverse.jsonschema.builder.MutableSchema
import io.mverse.jsonschema.defaultSchemaReader
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.loading.parseJsrObject
import io.mverse.jsonschema.schemaBuilder
import io.mverse.jsonschema.validation.ValidationTestSupport.expectSuccess
import lang.json.toJsrValue
import lang.json.unboxAsAny
import org.junit.Assert
import org.junit.Test

class EmptySchemaTest {

  @Test
  fun testAllGenericProps() {
    val actual = roundTripSchema("my title", "my description", "my/id")
    assertThat(actual.keys).containsAll("title", "description", "\$id")
  }

  @Test
  fun testBuilder() {
    Assert.assertEquals(schemaBuilder{}.build(), schemaBuilder{}.build())
  }

  @Test
  fun testOnlyId() {
    val actual = roundTripSchema(id = "my/id")
    assertThat(actual.keys).hasSize(1)
    assertThat(actual[Keywords.DOLLAR_ID_KEY]?.unboxAsAny()).isEqualTo("my/id")
  }

  @Test
  fun testOnlySchemaDescription() {
    val actual = roundTripSchema(description = "descr")
    assertThat(actual.keys).hasSize(1)
    assertThat(actual["description"]?.unboxAsAny()).isEqualTo("descr")
  }

  @Test
  fun testOnlyTitle() {
    val actual = roundTripSchema("my title", null, null)
    assertThat(actual.keys).containsAll("title")
    assertThat(actual["title"]?.unboxAsAny()).isEqualTo("my title")
  }

  @Test
  fun testToString() {
    assertThat(schemaBuilder{}.build().toString()).isEqualIgnoringWhitespace("{}")
  }

  @Test
  fun testValidate() {
    expectSuccess { ValidationMocks.createTestValidator(schemaBuilder{}.build()).validate("something".toJsrValue()) }
  }

  private fun roundTripSchema(title: String? = null,
                              description: String? = null,
                              id: String? = null): lang.json.JsrObject {
    val builder: MutableSchema = if (id?.isNotBlank() == true) {
      JsonSchemas.schemaBuilder(id = id, loader = defaultSchemaReader)
    } else {
      schemaBuilder{}
    }
    if (title != null) builder.title = title
    if (description != null) builder.description = description
    return builder.build().toString().parseJsrObject()
  }
}
