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
import assertk.assertions.hasSize
import assertk.assertions.hasToString
import assertk.assertions.isEqualTo
import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.createSchemaReader
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.loading.parseJson
import io.mverse.jsonschema.loading.parseJsonObject
import io.mverse.jsonschema.minus
import io.mverse.jsonschema.resourceLoader
import io.mverse.jsonschema.validation.ValidationMocks.createTestValidator
import io.mverse.jsonschema.validation.ValidationMocks.mockBooleanSchema
import io.mverse.jsonschema.validation.ValidationMocks.mockNullSchema
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
import kotlinx.serialization.json.json
import lang.json.toJsonLiteral
import lang.plus
import lang.size
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ObjectKeywordsValidatorTest {

  private lateinit var objectTestCases: kotlinx.serialization.json.JsonObject

  @Before
  fun before() {
    objectTestCases = JsonSchema.resourceLoader().readJsonObject("objecttestcases.json")
  }

  @Test
  fun additionalPropertiesOnEmptyObject() {

    val input = objectTestCases.getObject("emptyObject")
    val testSchema = mockObjectSchema {
      schemaOfAdditionalProperties = mockBooleanSchema
    }

    expectSuccess { ValidationMocks.createTestValidator(testSchema).validate(input) }
  }

  @Test
  fun maxPropertiesFailure() {
    val subject = buildWithLocation(mockObjectSchema.apply { maxProperties = 2 })
    failureOf(subject)
        .input(objectTestCases.getObject("maxPropertiesFailure"))
        .expectedPointer("#")
        .expectedKeyword("maxProperties")
        .expect()
  }

  @Test
  fun minPropertiesFailure() {
    val subject = buildWithLocation(mockObjectSchema.apply { minProperties = 2 })
    failureOf(subject)
        .input(objectTestCases.getObject("minPropertiesFailure"))
        .expectedPointer("#")
        .expectedKeyword("minProperties")
        .expect()
  }

  @Test
  fun multipleSchemaDepViolation() {
    val billingAddressSchema = mockStringSchema
    val billingNameSchema = mockStringSchema.apply { minLength = 4 }
    val subject = mockObjectSchema {
      properties["name"] = mockStringSchema
      properties["credit_card"] = mockNumberSchema
      schemaDependencies += "credit_card" to mockObjectSchema.apply {
        properties["billing_address"] = billingAddressSchema
        properties["billing_name"] = billingNameSchema
        requiredProperties += "billing_address"
      }
      schemaDependencies += "name" to mockObjectSchema.apply {
        requiredProperties += "age"
      }
    }

    val e = verifyFailure { ValidationMocks.createTestValidator(subject).validate(objectTestCases["schemaDepViolation"]) }
    var creditCardFailure = e.causes[0]
    var ageFailure = e.causes[1]
    // due to schemaDeps being stored in (unsorted) HashMap, the exceptions may need to be swapped
    if (creditCardFailure.causes.isEmpty()) {
      val tmp = creditCardFailure
      creditCardFailure = ageFailure
      ageFailure = tmp
    }
    val billingAddressFailure = creditCardFailure.causes[0]
    assertEquals("#/billing_address", billingAddressFailure.pathToViolation)
    assertEquals(billingAddressSchema.build(), billingAddressFailure.violatedSchema)
    val billingNameFailure = creditCardFailure
        .causes[1]
    assertEquals("#/billing_name", billingNameFailure.pathToViolation)
    assertEquals(billingNameSchema.build(), billingNameFailure.violatedSchema)
    assertEquals("#", ageFailure.pathToViolation)
    assertEquals("#: required key [age] not found", ageFailure.message)
  }

  @Test
  fun multipleViolations() {
    val subject = mockObjectSchema {
      properties["numberProp"] = mockNumberSchema
      properties["boolProp"] = mockBooleanSchema
      patternProperties["^string.*"] = mockStringSchema
      requiredProperties += "boolProp"
    }

    val e = verifyFailure { ValidationMocks.createTestValidator(subject).validate(objectTestCases.get("multipleViolations")) }

    assertEquals(3, e.causes.size)
    assertEquals(1, countCauseByJsonPointer(e, "#"))
    assertEquals(1, countCauseByJsonPointer(e, "#/numberProp"))
    assertEquals(1, countCauseByJsonPointer(e, "#/stringPatternMatch"))

    val messages = e.allMessages
    assertEquals(3, messages.size)
    assertEquals(1, countMatchingMessage(messages, "#:"))
    assertEquals(1, countMatchingMessage(messages, "#/numberProp:"))
    assertEquals(1, countMatchingMessage(messages, "#/stringPatternMatch:"))
  }

  @Test
  @Throws(Exception::class)
  fun multipleViolationsNested() {
    val newBuilder = {
      mockObjectSchema.apply {
        properties["numberProp"] = mockNumberSchema
        properties["boolProp"] = mockBooleanSchema
        patternProperties["^string.*"] = mockStringSchema
        requiredProperties += "boolProp"
      }
    }

    val nested2 = newBuilder()
    val nested1 = newBuilder().apply { properties["nested"] = nested2 }
    val subject = newBuilder().invoke { properties["nested"] = nested1 }

    val subjectException = verifyFailure { ValidationMocks.createTestValidator(subject).validate(objectTestCases.get("multipleViolationsNested")) }

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
    expectSuccess { ValidationMocks.createTestValidator(mockObjectSchema {}).validate(objectTestCases["noProperties"]) }
  }

  @Test
  fun notRequireObject() {
    expectSuccess {
      val objectSchema = mockSchema {}
      ValidationMocks.createTestValidator(objectSchema).validate("foo".toJsonLiteral())
    }
  }

  @Test
  fun patternPropertyOnEmptyObjct() {
    val schema = mockObjectSchema {
      patternProperties["b_.*"] = mockBooleanSchema
    }
    expectSuccess { ValidationMocks.createTestValidator(schema).validate(json {}) }
  }

  @Test
  fun patternPropertyOverridesAdditionalPropSchema() {
    val schema = mockObjectSchema {
      schemaOfAdditionalProperties = mockNumberSchema
      patternProperties["aa.*"] = mockBooleanSchema
    }
    expectSuccess { ValidationMocks.createTestValidator(schema).validate(objectTestCases["patternPropertyOverridesAdditionalPropSchema"]) }
  }

  @Test
  fun patternPropertyViolation() {
    val subject = mockObjectSchema.build {
      patternProperties["^b_.*"] = mockBooleanSchema
      patternProperties["^s_.*"] = mockStringSchema
    }
    expectFailure(subject, mockBooleanSchema.build(), "#/b_1",
        objectTestCases["patternPropertyViolation"])
  }

  @Test
  fun patternPropsOverrideAdditionalProps() {
    val schema = mockObjectSchema.build {
      patternProperties["^v.*"] = mockSchema
      schemaOfAdditionalProperties = mockBooleanSchema.apply { constValue = false.toJsonLiteral() }

    }
    expectSuccess { createTestValidator(schema).validate(objectTestCases["patternPropsOverrideAdditionalProps"]) }
  }

  @Test
  fun propertyDepViolation() {
    mockObjectSchema {
      properties["ifPresent"] = mockNullSchema
      properties["mustBePresent"] = mockBooleanSchema
    }
    val subject = mockObjectSchema {
      properties["ifPresent"] = mockNullSchema
      propertyDependencies += "ifPresent" to "mustBePresent"
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
        .input(objectTestCases.getObject("propertyNameSchemaViolation"))
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
    val subject = mockObjectSchema.build() {
       properties["boolProp"] = mockBooleanSchema
    }

    expectFailure(subject, mockBooleanSchema.build(), "#/boolProp",
        objectTestCases.get("propertySchemaViolation"))
  }

  @Test
  fun requireObject() {
    expectFailure(mockObjectSchema.build {}, "#", "foo".toJsonLiteral())
  }

  @Test
  fun requiredProperties() {
    val subject = mockObjectSchema {
      properties.set("boolProp", mockBooleanSchema)
      properties.set("nullProp", mockNullSchema)
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
    val billingAddressSchema = mockStringSchema

    val schemaBuilder = mockObjectSchema.apply {
      properties["billing_address"] = billingAddressSchema
      properties["name"] = mockStringSchema
      properties["credit_card"] = mockNumberSchema
    }

    val subject = schemaBuilder.build {
      schemaDependencies += "credit_card" to mockObjectSchema.apply {
        requiredProperties += "billing_address"
      }
    }

    expectFailure(subject, billingAddressSchema.build(), "#/billing_address",
        objectTestCases.get("schemaDepViolation"))
  }

  @Test
  fun schemaPointerIsPassedToValidationError() {
    val subject = mockObjectSchema("#/dependencies/a").invoke {
      minProperties = 1
    }

    val e = verifyFailure {
      val testValue = 1.toJsonLiteral()
      ValidationMocks.createTestValidator(subject).validate(testValue)
    }
    assert(e.schemaLocation).hasToString("#/dependencies/a")
  }

  @Test
  fun testImmutability() {
    val builder = mockObjectSchema.apply {
      propertyDependencies += "a" to "b"
      schemaDependencies += "a" to mockBooleanSchema
      patternProperties["aaa"] = mockBooleanSchema
    }

    val schema = builder.build().asDraft6()

    assert(schema.patternProperties).hasSize(1)
    assert(schema.propertyDependencies.size).isEqualTo(1)
    assert(schema.propertySchemaDependencies).hasSize(1)

    builder.apply {
      propertyDependencies += ("c" to "a")
      schemaDependencies += "b" to mockBooleanSchema
      patternProperties["bbb"] = mockBooleanSchema
    }
    assertEquals(1, schema.propertyDependencies.size)
    assertEquals(1, schema.propertySchemaDependencies.size)
    assertEquals(1, schema.patternProperties.size)
  }

  @Test
  fun toStringNoAdditionalProperties() {
    val rawSchemaJson = readResourceAsJson("tostring/objectschema.json")
    val actual = JsonSchema.createSchemaReader().readSchema(rawSchemaJson).toString()
    assertEquals(rawSchemaJson, actual.parseJsonObject())
  }

  @Test
  fun toStringNoExplicitType() {
    val rawSchemaJson = readResourceAsJson("tostring/objectschema.json") - "type"
    val actual = JsonSchema.createSchemaReader().readSchema(rawSchemaJson).toString()
    assertEquals(rawSchemaJson, actual.parseJson())
  }

  @Test
  fun toStringSchemaDependencies() {
    val rawSchemaJson = readResourceAsJson("tostring/objectschema-schemadep.json")
    val actual = JsonSchema.createSchemaReader().readSchema(rawSchemaJson).toString()
    assertEquals(rawSchemaJson, actual.parseJson())
  }

  @Test
  fun toStringTest() {
    val rawSchemaJson = readResourceAsJson("tostring/objectschema.json")
    val actual = JsonSchema.createSchemaReader().readSchema(rawSchemaJson).toString()
    assertEquals(rawSchemaJson, actual.parseJson())
  }

  @Test
  fun typeFailure() {
    failureOf(mockObjectSchema {})
        .expectedKeyword("type")
        .input("a")
        .expect()
  }

  internal fun readResourceAsJson(url: String): kotlinx.serialization.json.JsonObject {
    return JsonSchema.resourceLoader().readJsonObject(url)
  }
}
