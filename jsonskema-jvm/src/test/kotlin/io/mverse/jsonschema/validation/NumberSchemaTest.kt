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

import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.getValidator
import io.mverse.jsonschema.loading.parseJsonObject
import io.mverse.jsonschema.minus
import io.mverse.jsonschema.plus
import io.mverse.jsonschema.resourceLoader
import io.mverse.jsonschema.schemaReader
import io.mverse.jsonschema.validation.ValidationMocks.mockIntegerSchema
import io.mverse.jsonschema.validation.ValidationMocks.mockNumberSchema
import io.mverse.jsonschema.validation.ValidationMocks.mockSchema
import io.mverse.jsonschema.validation.ValidationTestSupport.expectSuccess
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import lang.json.toJsonLiteral
import org.junit.Assert.assertEquals
import org.junit.Test

class NumberSchemaTest {

  private val loader = JsonSchema.resourceLoader()

  @Test
  fun exclusiveMaximum() {
    val subject = mockNumberSchema().exclusiveMaximum(20).build()
    ValidationTestSupport.failureOf(subject)
        .expectedKeyword("exclusiveMaximum")
        .input(20)
        .expect()
  }

  @Test
  fun exclusiveMinimum() {
    val subject = ValidationTestSupport.buildWithLocation(mockNumberSchema().exclusiveMinimum(10.0))
    ValidationTestSupport.failureOf(subject)
        .expectedKeyword("exclusiveMinimum")
        .input(10)
        .expect()
  }

  @Test
  fun longNumber() {
    val schema = mockNumberSchema().build()
    JsonSchema.getValidator(schema).validate(4278190207L.toJsonLiteral())
  }

  @Test
  fun maximum() {
    val subject = ValidationTestSupport.buildWithLocation(mockNumberSchema().maximum(20.0))
    ValidationTestSupport.failureOf(subject)
        .expectedKeyword("maximum")
        .input(21)
        .expect()
  }

  @Test
  fun minimumFailure() {
    val subject = ValidationTestSupport.buildWithLocation(mockNumberSchema().minimum(10.0))
    ValidationTestSupport.failureOf(subject)
        .expectedKeyword("minimum")
        .input(9)
        .expect()
  }

  @Test
  fun multipleOfFailure() {
    val subject = ValidationTestSupport.buildWithLocation(mockNumberSchema().multipleOf(10))
    ValidationTestSupport.failureOf(subject)
        .expectedKeyword("multipleOf")
        .input(15)
        .expect()
  }

  @Test
  fun notRequiresNumber() {
    val numberSchema = mockSchema()
        .build()
    expectSuccess { JsonSchema.getValidator(numberSchema).validate("foo".toJsonLiteral()) }
  }

  @Test
  fun requiresIntegerSuccess() {
    val numberSchema = mockNumberSchema().build()
    expectSuccess { JsonSchema.getValidator(numberSchema).validate(10.toJsonLiteral()) }
  }

  @Test
  fun requiresIntegerFailure() {
    val subject = mockIntegerSchema().build()
    ValidationTestSupport.expectFailure(subject, 10.2f.toJsonLiteral())
  }

  @Test
  fun smallMultipleOf() {
    val schema = mockNumberSchema()
        .multipleOf(0.0001)
        .build()
    JsonSchema.getValidator(schema).validate(0.0075.toJsonLiteral())
  }

  @Test
  fun success() {
    val schema = mockNumberSchema()
        .minimum(10.0)
        .exclusiveMaximum(11.0)
        .multipleOf(10)
        .build()
    JsonSchema.getValidator(schema).validate(10.0.toJsonLiteral())
  }

  @Test
  fun toStringNoExplicitType() {
    val rawSchemaJson = loader.readJsonObject("tostring/numberschema.json") - "type"
    val actual = JsonSchema.schemaReader().readSchema(rawSchemaJson).toString()
    assertEquals(rawSchemaJson, actual.parseJsonObject())
  }

  @Test
  fun toStringReqInteger() {
    val rawSchemaJson: kotlinx.serialization.json.JsonObject = loader.readJsonObject("tostring/numberschema.json") + ("type" to "number".toJsonLiteral())
    val actual = JsonSchema.schemaReader().readSchema(rawSchemaJson).toString()
    assertEquals(rawSchemaJson, actual.parseJsonObject())
  }

  @Test
  fun toStringTest() {
    val rawSchemaJson = loader.readJsonObject("tostring/numberschema.json")
    val actual = JsonSchema.schemaReader().readSchema(rawSchemaJson).toString()
    assertEquals(rawSchemaJson, actual.parseJsonObject())
  }

  @Test
  fun typeFailure() {
    ValidationTestSupport.failureOf(mockNumberSchema())
        .expectedKeyword("type")
        .input(JsonNull)
        .expect()
  }
}
