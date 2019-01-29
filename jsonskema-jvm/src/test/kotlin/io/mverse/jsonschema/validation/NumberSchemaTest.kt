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
import io.mverse.jsonschema.assertj.asserts.isNotValid
import io.mverse.jsonschema.assertj.asserts.validating
import io.mverse.jsonschema.createSchemaReader
import io.mverse.jsonschema.getValidator
import io.mverse.jsonschema.keyword.Keywords.EXCLUSIVE_MAXIMUM
import io.mverse.jsonschema.loading.parseJsrObject
import io.mverse.jsonschema.minus
import io.mverse.jsonschema.plus
import io.mverse.jsonschema.resourceLoader
import io.mverse.jsonschema.validation.ValidationMocks.mockIntegerSchema
import io.mverse.jsonschema.validation.ValidationMocks.mockNumberSchema
import io.mverse.jsonschema.validation.ValidationMocks.mockSchema
import io.mverse.jsonschema.validation.ValidationTestSupport.expectSuccess
import lang.json.JsrNull
import lang.json.JsrObject
import lang.json.jsrNumber
import lang.json.toJsrValue
import org.junit.Assert.assertEquals
import org.junit.Test

class NumberSchemaTest {

  private val loader = JsonSchemas.resourceLoader()

  @Test
  fun exclusiveMaximum() {
    val schema = mockNumberSchema.build { exclusiveMaximum = (20) }
    schema.validating(jsrNumber(20))
        .isNotValid()
        .hasKeyword(EXCLUSIVE_MAXIMUM)
  }

  @Test
  fun exclusiveMinimum() {
    val subject = ValidationTestSupport.buildWithLocation(mockNumberSchema.apply { exclusiveMinimum = (10.0) })
    ValidationTestSupport.failureOf(subject)
        .expectedKeyword("exclusiveMinimum")
        .input(10)
        .expect()
  }

  @Test
  fun longNumber() {
    val schema = mockNumberSchema.build()
    JsonSchemas.getValidator(schema).validate(4278190207L.toJsrValue())
  }

  @Test
  fun maximum() {
    val subject = ValidationTestSupport.buildWithLocation(mockNumberSchema { maximum = (20.0) })
    ValidationTestSupport.failureOf(subject)
        .expectedKeyword("maximum")
        .input(21)
        .expect()
  }

  @Test
  fun minimumFailure() {
    val subject = ValidationTestSupport.buildWithLocation(mockNumberSchema { minimum = 10.0 })
    ValidationTestSupport.failureOf(subject)
        .expectedKeyword("minimum")
        .input(9)
        .expect()
  }

  @Test
  fun multipleOfFailure() {
    val subject = ValidationTestSupport.buildWithLocation(mockNumberSchema { multipleOf = 10 })
    ValidationTestSupport.failureOf(subject)
        .expectedKeyword("multipleOf")
        .input(15)
        .expect()
  }

  @Test
  fun notRequiresNumber() {
    val numberSchema = mockSchema.build()
    expectSuccess { JsonSchemas.getValidator(numberSchema).validate("foo".toJsrValue()) }
  }

  @Test
  fun requiresIntegerSuccess() {
    val numberSchema = mockNumberSchema.build()
    expectSuccess { JsonSchemas.getValidator(numberSchema).validate(10.toJsrValue()) }
  }

  @Test
  fun requiresIntegerFailure() {
    val subject = mockIntegerSchema.build()
    ValidationTestSupport.expectFailure(subject, jsrNumber(10.2))
  }

  @Test
  fun smallMultipleOf() {
    val schema = mockNumberSchema.build {
      multipleOf = 0.0001
    }

    JsonSchemas.getValidator(schema).validate(0.0075.toJsrValue())
  }

  @Test
  fun success() {
    val schema = mockNumberSchema.build {
      minimum = 10.0
      exclusiveMaximum = 11.0
      multipleOf = (10)
    }

    JsonSchemas.getValidator(schema).validate(10.0.toJsrValue())
  }

  @Test
  fun toStringNoExplicitType() {
    val rawSchemaJson = loader.readJsonObject("tostring/numberschema.json") - "type"
    val schemaFromJson = JsonSchemas.createSchemaReader().readSchema(rawSchemaJson)
    val actual = schemaFromJson.toString()
    assertEquals(rawSchemaJson, actual.parseJsrObject())
  }

  @Test
  fun toStringReqInteger() {
    val rawSchemaJson: JsrObject = loader.readJsonObject("tostring/numberschema.json") + ("type" to "number".toJsrValue())
    val actual = JsonSchemas.createSchemaReader().readSchema(rawSchemaJson).toString()
    assertEquals(rawSchemaJson, actual.parseJsrObject())
  }

  @Test
  fun toStringTest() {
    val rawSchemaJson = loader.readJsonObject("tostring/numberschema.json")
    val actual = JsonSchemas.createSchemaReader().readSchema(rawSchemaJson).toString()
    assertEquals(rawSchemaJson, actual.parseJsrObject())
  }

  @Test
  fun typeFailure() {
    ValidationTestSupport.failureOf(mockNumberSchema)
        .expectedKeyword("type")
        .input(JsrNull)
        .expect()
  }
}
