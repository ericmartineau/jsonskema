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
import assertk.assertAll
import assertk.assertions.hasSize
import assertk.assertions.hasToString
import assertk.assertions.isEqualTo
import io.mverse.json.jsr353.raw
import io.mverse.jsonschema.JsonSchemas
import io.mverse.jsonschema.assertj.asserts.hasViolationAt
import io.mverse.jsonschema.assertj.asserts.hasViolationCount
import io.mverse.jsonschema.assertj.asserts.isNotValid
import io.mverse.jsonschema.assertj.asserts.validating
import io.mverse.jsonschema.createSchemaReader
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.loading.parseJsrJson
import io.mverse.jsonschema.loading.parseJsrObject
import io.mverse.jsonschema.minus
import io.mverse.jsonschema.resourceLoader
import io.mverse.jsonschema.schema
import io.mverse.jsonschema.schemaBuilder
import io.mverse.jsonschema.validation.ValidationMocks.createTestValidator
import io.mverse.jsonschema.validation.ValidationMocks.mockBooleanSchema
import io.mverse.jsonschema.validation.ValidationMocks.mockNumberSchema
import io.mverse.jsonschema.validation.ValidationMocks.mockObjectSchema
import io.mverse.jsonschema.validation.ValidationMocks.mockSchema
import io.mverse.jsonschema.validation.ValidationMocks.mockStringSchema
import io.mverse.jsonschema.validation.ValidationTestSupport.buildWithLocation
import io.mverse.jsonschema.validation.ValidationTestSupport.countCauseByJsonPointer
import io.mverse.jsonschema.validation.ValidationTestSupport.countMatchingMessage
import io.mverse.jsonschema.validation.ValidationTestSupport.expectFailure
import io.mverse.jsonschema.validation.ValidationTestSupport.expectSuccess
import io.mverse.jsonschema.validation.ValidationTestSupport.failureOf
import io.mverse.jsonschema.validation.ValidationTestSupport.verifyFailure
import lang.collection.MutableSetMultimap
import lang.collection.plus
import lang.json.JsrValue
import lang.json.jsrObject
import lang.json.toJsrValue
import lang.json.toPrettyString
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ObjectKeywordsValidatorTest {

  private lateinit var objectTestCases: lang.json.JsrObject

  @Before
  fun before() {
    objectTestCases = JsonSchemas.resourceLoader().readJsonObject("objecttestcases.json")
  }

  @Test
  fun additionalPropertiesOnEmptyObject() {

    val input: JsrValue = objectTestCases.raw["emptyObject"]
    val testSchema = mockObjectSchema {
      schemaOfAdditionalProperties = mockBooleanSchema
    }

    expectSuccess { ValidationMocks.createTestValidator(testSchema).validate(input) }
  }

  @Test
  fun maxPropertiesFailure() {
    val subject = buildWithLocation(mockObjectSchema.apply { maxProperties = 2 })
    failureOf(subject)
        .input(objectTestCases.raw.get<JsrValue>("maxPropertiesFailure"))
        .expectedPointer("#")
        .expectedKeyword("maxProperties")
        .expect()
  }

  @Test
  fun minPropertiesFailure() {
    val subject = buildWithLocation(mockObjectSchema.apply { minProperties = 2 })
    failureOf(subject)
        .input(objectTestCases.raw.get<JsrValue>("minPropertiesFailure"))
        .expectedPointer("#")
        .expectedKeyword("minProperties")
        .expect()
  }

  @Test
  fun multipleSchemaDepViolation() {
    val subject = schema {
      properties {
        "name" required string
        "credit_card" required number
        "age" optional number
      }
      schemaDependencies["credit_card"] = {
        properties {
          "billing_address" required string
          "billing_name" optional string {
            minLength = 4
          }
        }
      }
      schemaDependencies["name"] = {
        requiredProperties += "age"
      }
    }

    val depViolation = objectTestCases["schemaDepViolation"]
    println(depViolation?.toPrettyString())
    subject.validating(depViolation)
        .isNotValid {
          hasViolationAt("#/billing_address")
          hasViolationAt("#/billing_name")
          hasViolationAt("#")
        }
  }

  @Test
  fun multipleViolations() {
    val subject = schema {
      properties["numberProp"] = {type = JsonSchemaType.NUMBER}
      properties["boolProp"] = {type = JsonSchemaType.BOOLEAN }
      patternProperties["^string.*"] = {type = JsonSchemaType.STRING}
      requiredProperties += "boolProp"
    }

    val multiple = jsrObject {
      "numberProp" *= "not number"
      "stringPatternMatch" *= 2
      "nested" *= jsrObject {
        "numberProp" *= "not number 1"
        "stringPatternMatch" *= 11
        "nested" *= jsrObject {
          "numberProp" *= "not number 2"
          "stringPatternMatch" *= 22
        }
      }
    }

    subject.validating(multiple)
        .isNotValid {
          hasViolationCount(3)
          hasViolationAt("#")
          hasViolationAt("#/numberProp")
          hasViolationAt("#/stringPatternMatch")
        }
  }

  @Test
  fun multipleViolationsNested() {
    val subject = schemaBuilder {
      properties["numberProp"] = { type = JsonSchemaType.NUMBER }
      properties["boolProp"] = { type = JsonSchemaType.BOOLEAN }
      patternProperties["^string.*"] = { type = JsonSchemaType.STRING }
      requiredProperties += "boolProp"
      properties["nested"] = {
        properties["numberProp"] = { type = JsonSchemaType.NUMBER }
        properties["boolProp"] = { type = JsonSchemaType.BOOLEAN }
        patternProperties["^string.*"] = { type = JsonSchemaType.STRING }
        requiredProperties += "boolProp"
        properties["nested"] = {
          properties["numberProp"] = { type = JsonSchemaType.NUMBER }
          properties["boolProp"] = { type = JsonSchemaType.BOOLEAN }
          patternProperties["^string.*"] = { type = JsonSchemaType.STRING }
          requiredProperties += "boolProp"
        }
      }
    }.build()

    val multiple = jsrObject {
      "numberProp" *= "not number"
      "stringPatternMatch" *= 2
      "nested" *= jsrObject {
        "numberProp" *= "not number 1"
        "stringPatternMatch" *= 11
        "nested" *= jsrObject {
          "numberProp" *= "not number 2"
          "stringPatternMatch" *= 22
        }
      }
    }

    val v = subject
        .validating(multiple)

    val subjectException = v.actual!!

    assertEquals("#: 9 schema violations found", subjectException.message)
    assertEquals(4, subjectException.causes.size)
    assertEquals(1, countCauseByJsonPointer(subjectException, "#"))
    assertEquals(1, countCauseByJsonPointer(subjectException, "#/numberProp"))
    assertEquals(1, countCauseByJsonPointer(subjectException, "#/stringPatternMatch"))
    assertEquals(1, countCauseByJsonPointer(subjectException, "#/nested"))

    val nested1Exception = subjectException.causes
        .firstOrNull { ex -> ex.pathToViolation.equals("#/nested") }!!

    assertEquals("#/nested: 6 schema violations found", nested1Exception.message)
    assertEquals(4, nested1Exception.causes.size)
    assertEquals(1, countCauseByJsonPointer(nested1Exception, "#/nested"))
    assertEquals(1, countCauseByJsonPointer(nested1Exception, "#/nested/numberProp"))
    assertEquals(1, countCauseByJsonPointer(nested1Exception, "#/nested/stringPatternMatch"))
    assertEquals(1, countCauseByJsonPointer(nested1Exception, "#/nested/nested"))

    val nested2Exception = nested1Exception.causes.stream()
        .filter { ex -> ex.pathToViolation.equals("#/nested/nested") }
        .findFirst()
        .get()
    assertEquals("#/nested/nested: 3 schema violations found", nested2Exception.message)
    assertEquals(3, nested2Exception.causes.size)
    assertEquals(1, countCauseByJsonPointer(nested2Exception, "#/nested/nested"))
    assertEquals(1, countCauseByJsonPointer(nested2Exception, "#/nested/nested/numberProp"))
    assertEquals(1, countCauseByJsonPointer(nested2Exception, "#/nested/nested/stringPatternMatch"))

    val messages = subjectException.allMessages
    assertEquals(9, messages.size)
    assertEquals(1, countMatchingMessage(messages, "#:"))
    assertEquals(1, countMatchingMessage(messages, "#/numberProp:"))
    assertEquals(1, countMatchingMessage(messages, "#/stringPatternMatch:"))
    assertEquals(1, countMatchingMessage(messages, "#/nested:"))
    assertEquals(1, countMatchingMessage(messages, "#/nested/numberProp:"))
    assertEquals(1, countMatchingMessage(messages, "#/nested/stringPatternMatch:"))
    assertEquals(1, countMatchingMessage(messages, "#/nested/nested:"))
    assertEquals(1, countMatchingMessage(messages, "#/nested/nested/numberProp:"))
    assertEquals(1, countMatchingMessage(messages, "#/nested/nested/stringPatternMatch:"))
  }

  @Test
  fun noProperties() {
    expectSuccess { ValidationMocks.createTestValidator(mockObjectSchema {}).validate(objectTestCases.raw.get<JsrValue>("noProperties")) }
  }

  @Test
  fun notRequireObject() {
    expectSuccess {
      val objectSchema = mockSchema {}
      ValidationMocks.createTestValidator(objectSchema).validate("foo".toJsrValue())
    }
  }

  @Test
  fun patternPropertyOnEmptyObjct() {
    val schema = mockObjectSchema.apply {
      patternProperties["b_.*"] = mockBooleanSchema
    }
    expectSuccess { ValidationMocks.createTestValidator(schema).validate(jsrObject {}) }
  }

  @Test
  fun patternPropertyOverridesAdditionalPropSchema() {
    val schema = mockObjectSchema {
      schemaOfAdditionalProperties = mockNumberSchema
      patternProperties["aa.*"] = mockBooleanSchema
    }
    expectSuccess { ValidationMocks.createTestValidator(schema).validate(objectTestCases["patternPropertyOverridesAdditionalPropSchema"] as JsrValue) }
  }

  @Test
  fun patternPropertyViolation() {
    val subject = schema {
      patternProperties["^b_.*"] = {type = JsonSchemaType.BOOLEAN}
      patternProperties["^s_.*"] = {type = JsonSchemaType.STRING}
    }
    subject.validating(objectTestCases["patternPropertyViolation"] as JsrValue)
        .isNotValid {
          hasViolationAt("#/b_1")
        }
  }

  @Test
  fun patternPropsOverrideAdditionalProps() {
    val schema = mockObjectSchema.build {
      patternProperties["^v.*"] = mockSchema
      schemaOfAdditionalProperties = mockBooleanSchema.apply { constValue = false.toJsrValue() }

    }
    expectSuccess { createTestValidator(schema).validate(objectTestCases["patternPropsOverrideAdditionalProps"] as JsrValue) }
  }

  @Test
  fun propertyDepViolation() {
    mockObjectSchema {
      properties["ifPresent"] = {type = JsonSchemaType.NULL}
      properties["mustBePresent"] = {type = JsonSchemaType.BOOLEAN}
    }
    val subject = mockObjectSchema {
      properties["ifPresent"] = {type = JsonSchemaType.NULL}
      propertyDependencies += MutableSetMultimap<String, String>()
          .apply {
            add("ifPresent", "mustBePresent")
          }
    }

    failureOf(subject)
        .input(objectTestCases["propertyDepViolation"])
        .expectedKeyword("dependencies")
        .expect()
  }

  @Test
  fun propertyNameSchemaSchemaViolation() {
    val testSchema = mockStringSchema.apply { pattern = "^[a-z_]{3,8}$" }
    val subject = mockObjectSchema {
      propertyNameSchema = testSchema
    }
    failureOf(subject)
        .input(objectTestCases["propertyNameSchemaViolation"] as JsrValue)
        .expectedConsumer { error ->
          Assert.assertEquals("#", error.schemaLocation.toString())
          Assert.assertEquals(3, error.violationCount)
        }
        .expectedKeyword(Keywords.PROPERTY_NAMES)
        .expectedSchemaLocation("#")
        .expect()
  }

  @Test
  fun propertySchemaViolation() {
    val subject = mockObjectSchema.build {
      properties["boolProp"] = {
        type = JsonSchemaType.BOOLEAN
      }
    }

    expectFailure(subject, mockBooleanSchema.build(), "#/boolProp",
        objectTestCases.get("propertySchemaViolation") as JsrValue)
  }

  @Test
  fun requireObject() {
    expectFailure(mockObjectSchema.build {}, "#", "foo".toJsrValue())
  }

  @Test
  fun requiredProperties() {
    val subject = mockObjectSchema {
      properties["boolProp"] = {type = JsonSchemaType.BOOLEAN}
      properties["nullProp"] = {type = JsonSchemaType.NULL}
      requiredProperties += "boolProp"
    }

    failureOf(subject)
        .expectedPointer("#")
        .expectedKeyword("required")
        .input(objectTestCases["requiredProperties"])
        .expect()
  }

  @Test
  fun schemaDepViolation() {
    schema {
      properties {
        "billing_address" required string
        "name" required { type = JsonSchemaType.STRING }
        "credit_card" optional { type = JsonSchemaType.NUMBER }
      }
    }.validating(objectTestCases["schemaDepViolation"] as JsrValue)
        .hasViolationAt("#/billing_address")
  }

  @Test
  fun schemaPointerIsPassedToValidationError() {
    val subject = mockObjectSchema("#/dependencies/a").invoke {
      minProperties = 1
    }

    val e = verifyFailure {
      val testValue = 1.toJsrValue()
      ValidationMocks.createTestValidator(subject).validate(testValue)
    }
    assert(e.schemaLocation).hasToString("#/dependencies/a")
  }

  @Test
  fun testImmutability() {
    val builder = schemaBuilder {
      propertyDependencies += ("a" to "b")
      schemaDependencies["a"] = { type = JsonSchemaType.BOOLEAN }
      patternProperties["aaa"] = mockBooleanSchema
    }

    val schema = builder.build().draft6()

    assert(schema.patternProperties).hasSize(1)
    assert(schema.propertyDependencies.size()).isEqualTo(1)
    assert(schema.propertySchemaDependencies).hasSize(1)

    builder.apply {
      propertyDependencies += ("c" to "a")
      schemaDependencies["b"] = { type = JsonSchemaType.BOOLEAN }
      patternProperties["bbb"] = mockBooleanSchema
    }

    assertAll {
      assert(schema.propertyDependencies.size(), "propertyDependencies").isEqualTo(1)
      assert(schema.propertySchemaDependencies, "propertySchemaDependencies size").hasSize(1)
      assert(schema.patternProperties.size, "patternProperties").isEqualTo(1)
    }
  }

  @Test
  fun toStringNoAdditionalProperties() {
    val rawSchemaJson = readResourceAsJson("tostring/objectschema.json")
    val actual = JsonSchemas.createSchemaReader().readSchema(rawSchemaJson).toString()
    assertEquals(rawSchemaJson, actual.parseJsrObject())
  }

  @Test
  fun toStringNoExplicitType() {
    val rawSchemaJson = readResourceAsJson("tostring/objectschema.json") - "type"
    val actual = JsonSchemas.createSchemaReader().readSchema(rawSchemaJson).toString()
    assertEquals(rawSchemaJson, actual.parseJsrJson())
  }

  @Test
  fun toStringSchemaDependencies() {
    val rawSchemaJson = readResourceAsJson("tostring/objectschema-schemadep.json")
    val actual = JsonSchemas.createSchemaReader().readSchema(rawSchemaJson).toString()
    assertEquals(rawSchemaJson, actual.parseJsrJson())
  }

  @Test
  fun toStringTest() {
    val rawSchemaJson = readResourceAsJson("tostring/objectschema.json")
    val actual = JsonSchemas.createSchemaReader().readSchema(rawSchemaJson).toString()
    assertEquals(rawSchemaJson, actual.parseJsrJson())
  }

  @Test
  fun typeFailure() {
    failureOf(mockObjectSchema {})
        .expectedKeyword("type")
        .input("a")
        .expect()
  }

  internal fun readResourceAsJson(url: String): lang.json.JsrObject {
    return JsonSchemas.resourceLoader().readJsonObject(url)
  }
}
