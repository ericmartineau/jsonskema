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
import io.mverse.jsonschema.createSchemaReader
import io.mverse.jsonschema.loading.parseJsrObject
import io.mverse.jsonschema.minus
import io.mverse.jsonschema.resourceLoader
import io.mverse.jsonschema.validation.ValidationMocks.mockSchema
import io.mverse.jsonschema.validation.ValidationMocks.mockStringSchema
import io.mverse.jsonschema.validation.ValidationTestSupport.buildWithLocation
import io.mverse.jsonschema.validation.ValidationTestSupport.expectSuccess
import io.mverse.jsonschema.validation.ValidationTestSupport.failureOf
import io.mverse.jsonschema.validation.ValidationTestSupport.verifyFailure
import lang.json.toJsrValue
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test

class StringSchemaTest {

  private val validatorFactory = SchemaValidatorFactoryImpl.builder()
      .addCustomFormatValidator("test-format-failure") { "violation" }
      .addCustomFormatValidator("test-format-success") { null }
      .build()

  @Test
  fun formatFailure() {
    val schemaValidator = validatorFactory.createValidator(
        buildWithLocation(mockStringSchema { format = ("test-format-failure") })
    )
    failureOf(schemaValidator)
        .input("string")
        .expect()
  }

  @Test
  fun formatSuccess() {
    val schemaValidator = validatorFactory
        .createValidator(mockStringSchema.build { format = "test-format-success" })
    expectSuccess { schemaValidator.validate("string".toJsrValue()) }
  }

  fun issue38Pattern() {
    val schema = mockStringSchema.build { pattern = "\\+?\\d+" }
    val validator = ValidationMocks.createTestValidator(schema)
    verifyFailure { validator.validate("aaa".toJsrValue()) }
  }

  @Test
  fun maxLength() {
    val subject = buildWithLocation(mockStringSchema { maxLength = 3 })
    failureOf(subject)
        .expectedKeyword("maxLength")
        .input("foobar")
        .expect()
  }

  @Test
  fun minLength() {
    val subject = buildWithLocation(mockStringSchema { minLength = 2 })
    failureOf(subject)
        .expectedKeyword("minLength")
        .input("a")
        .expect()
  }

  @Test
  fun multipleViolations() {
    val schema = mockStringSchema {
      minLength = (3)
      maxLength = (1)
      pattern = ("^b.*")
    }
    failureOf(schema)
        .input("ab")
        .expectedConsumer { e -> Assert.assertEquals(3, e.causes.size) }
        .expect()
  }

  @Test
  fun notRequiresString() {
    val schema = mockSchema.build {}
    expectSuccess(schema, 2)
  }

  @Test
  fun patternFailure() {
    val subject = buildWithLocation(mockStringSchema { pattern = ("^a*$") })
    failureOf(subject).expectedKeyword("pattern").input("abc").expect()
  }

  @Test
  fun patternSuccess() {

    val schema = mockStringSchema.build { pattern = ("^a*$") }
    expectSuccess(schema, "aaaa")
  }

  @Test
  fun success() {

    expectSuccess(mockStringSchema.build {}, "foo")
  }

  @Test
  fun toStringNoExplicitType() {
    val rawSchemaJson = JsonSchema.resourceLoader().readJsonObject("tostring/stringschema.json") - "type"
    val schema = JsonSchema.createSchemaReader().readSchema(rawSchemaJson)
    val actual = JsonSchema.createSchemaReader().readSchema(schema.toString()).toString()
    assertEquals(rawSchemaJson, actual.parseJsrObject())
  }

  @Test
  fun toStringTest() {
    val rawSchemaJson = JsonSchema.resourceLoader().readJsonObject("tostring/stringschema.json")
    val actual = JsonSchema.createSchemaReader().readSchema(rawSchemaJson).toString()
    assertEquals(rawSchemaJson, actual.parseJsrObject())
  }

  @Test
  fun typeFailure() {
    failureOf(mockStringSchema)
        .expectedKeyword("type")
        .nullInput()
        .expect()
  }
}
