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
import io.mverse.jsonschema.enums.JsonSchemaType.BOOLEAN
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.loading.parseJson
import io.mverse.jsonschema.loading.parseJsonObject
import io.mverse.jsonschema.minus
import io.mverse.jsonschema.resourceLoader
import io.mverse.jsonschema.schemaBuilder
import io.mverse.jsonschema.schemaReader
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
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.json
import lang.json.toJsonLiteral
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
    val testSchema = mockObjectSchema()
        .schemaOfAdditionalProperties(mockBooleanSchema())
        .build()

    expectSuccess { ValidationMocks.createTestValidator(testSchema).validate(input) }
  }


  @Test
  fun maxPropertiesFailure() {
    val subject = buildWithLocation(mockObjectSchema().maxProperties(2))
    failureOf(subject)
        .input(objectTestCases.getObject("maxPropertiesFailure"))
        .expectedPointer("#")
        .expectedKeyword("maxProperties")
        .expect()
  }

  @Test
  fun minPropertiesFailure() {
    val subject = buildWithLocation(mockObjectSchema().minProperties(2))
    failureOf(subject)
        .input(objectTestCases.getObject("minPropertiesFailure"))
        .expectedPointer("#")
        .expectedKeyword("minProperties")
        .expect()
  }


  @Test
  fun multipleSchemaDepViolation() {
    val billingAddressSchema = mockStringSchema()
    val billingNameSchema = mockStringSchema().minLength(4)
    val subject = mockObjectSchema()
        .propertySchema("name", mockStringSchema())
        .propertySchema("credit_card", mockNumberSchema())
        .schemaDependency("credit_card", mockObjectSchema()
            .propertySchema("billing_address", billingAddressSchema)
            .requiredProperty("billing_address")
            .propertySchema("billing_name", billingNameSchema))
        .schemaDependency("name", mockObjectSchema()
            .requiredProperty("age"))
        .build()

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
    val subject = mockObjectSchema()
        .propertySchema("numberProp", mockNumberSchema())
        .patternProperty("^string.*", mockStringSchema())
        .propertySchema("boolProp", mockBooleanSchema())
        .requiredProperty("boolProp")
        .build()

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
      mockObjectSchema()
          .propertySchema("numberProp", mockNumberSchema())
          .patternProperty("^string.*", mockStringSchema())
          .propertySchema("boolProp", mockBooleanSchema())
          .requiredProperty("boolProp")
    }

    val nested2 = newBuilder()
    val nested1 = newBuilder().propertySchema("nested", nested2)
    val subject = newBuilder().propertySchema("nested", nested1).build()

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
    expectSuccess { ValidationMocks.createTestValidator(mockObjectSchema().build()).validate(objectTestCases.get("noProperties")) }
  }

  @Test
  fun notRequireObject() {
    expectSuccess {
      val objectSchema = mockSchema().build()
      ValidationMocks.createTestValidator(objectSchema).validate("foo".toJsonLiteral())
    }
  }

  @Test
  fun patternPropertyOnEmptyObjct() {
    val schema = mockObjectSchema()
        .patternProperty("b_.*", mockBooleanSchema())
        .build()
    expectSuccess { ValidationMocks.createTestValidator(schema).validate(json {}) }
  }

  @Test
  fun patternPropertyOverridesAdditionalPropSchema() {
    val schema = mockObjectSchema()
        .schemaOfAdditionalProperties(mockNumberSchema())
        .patternProperty("aa.*", mockBooleanSchema())
        .build()
    expectSuccess { ValidationMocks.createTestValidator(schema).validate(objectTestCases["patternPropertyOverridesAdditionalPropSchema"]) }
  }

  @Test
  fun patternPropertyViolation() {
    val subject = mockObjectSchema()
        .patternProperty("^b_.*", mockBooleanSchema())
        .patternProperty("^s_.*", mockStringSchema())
        .build()
    expectFailure(subject, mockBooleanSchema().build(), "#/b_1",
        objectTestCases["patternPropertyViolation"])
  }

  @Test
  fun patternPropsOverrideAdditionalProps() {
    val schema = mockObjectSchema()
        .patternProperty("^v.*", mockSchema())
        .schemaOfAdditionalProperties(
            mockBooleanSchema().constValue(false.toJsonLiteral())
        )
        .build()
    expectSuccess { ValidationMocks.createTestValidator(schema).validate(objectTestCases.get("patternPropsOverrideAdditionalProps")) }
  }

  @Test
  fun propertyDepViolation() {
    mockObjectSchema()
        .propertySchema("ifPresent", mockNullSchema())
        .propertySchema("mustBePresent", mockBooleanSchema())
    val subject = mockObjectSchema()
        .propertySchema("ifPresent", mockNullSchema())
        .propertyDependency("ifPresent", "mustBePresent").build()

    failureOf(subject)
        .input(objectTestCases["propertyDepViolation"])
        .expectedKeyword("dependencies")
        .expect()
  }

  @Test
  fun propertyNameSchemaSchemaViolation() {
    val propertyNameSchema = mockStringSchema().pattern("^[a-z_]{3,8}$")
    val subject = mockObjectSchema()
        .propertyNameSchema(propertyNameSchema)
        .build()
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
    val subject = mockObjectSchema()
        .propertySchema("boolProp", mockBooleanSchema())
        .build()
    expectFailure(subject, mockBooleanSchema().build(), "#/boolProp",
        objectTestCases.get("propertySchemaViolation"))
  }

  @Test
  fun requireObject() {
    expectFailure(mockObjectSchema().build(), "#", "foo".toJsonLiteral())
  }

  @Test
  fun requiredProperties() {
    val subject = mockObjectSchema().propertySchema("boolProp", mockBooleanSchema())
        .propertySchema("nullProp", mockNullSchema())
        .requiredProperty("boolProp")
        .build()

    failureOf(subject)
        .expectedPointer("#")
        .expectedKeyword("required")
        .input(objectTestCases["requiredProperties"])
        .expect()
  }

  @Test
  fun schemaDepViolation() {
    val billingAddressSchema = mockStringSchema()

    val schemaBuilder = mockObjectSchema()
    schemaBuilder.propertySchema("billing_address", billingAddressSchema)
    schemaBuilder.propertySchema("name", mockStringSchema())
    schemaBuilder.propertySchema("credit_card", mockNumberSchema())
    val subject = schemaBuilder
        .schemaDependency("credit_card", mockObjectSchema()
            .requiredProperty("billing_address"))
        .build()
    expectFailure(subject, billingAddressSchema.build(), "#/billing_address",
        objectTestCases.get("schemaDepViolation"))
  }

  @Test
  fun schemaPointerIsPassedToValidationError() {
    val subject = mockObjectSchema("#/dependencies/a")
        .minProperties(1)
        .build()
    val e = verifyFailure {
      val testValue = 1.toJsonLiteral()
      ValidationMocks.createTestValidator(subject).validate(testValue)
    }
    assert(e.schemaLocation).hasToString("#/dependencies/a")
  }

  @Test
  fun testImmutability() {
    val builder = mockObjectSchema()
    builder.propertyDependency("a", "b")
    builder.schemaDependency("a", mockBooleanSchema())
    builder.patternProperty("aaa", mockBooleanSchema())

    val schema = builder.build().asDraft6()
    assert(schema.patternProperties).hasSize(1)
    assert(schema.propertyDependencies.size).isEqualTo(1)
    assert(schema.propertySchemaDependencies).hasSize(1)
    builder.propertyDependency("c", "a")
    builder.schemaDependency("b", mockBooleanSchema())
    builder.patternProperty("bbb", mockBooleanSchema())
    assertEquals(1, schema.propertyDependencies.size)
    assertEquals(1, schema.propertySchemaDependencies.size)
    assertEquals(1, schema.patternProperties.size)
  }

  @Test
  fun toStringNoAdditionalProperties() {
    val rawSchemaJson = readResourceAsJson("tostring/objectschema.json")
    val actual = JsonSchema.schemaReader().readSchema(rawSchemaJson).toString()
    assertEquals(rawSchemaJson, actual.parseJsonObject())
  }

  @Test
  fun toStringNoExplicitType() {
    val rawSchemaJson = readResourceAsJson("tostring/objectschema.json") - "type"
    val actual = JsonSchema.schemaReader().readSchema(rawSchemaJson).toString()
    assertEquals(rawSchemaJson, actual.parseJson())
  }

  @Test
  fun toStringSchemaDependencies() {
    val rawSchemaJson = readResourceAsJson("tostring/objectschema-schemadep.json")
    val actual = JsonSchema.schemaReader().readSchema(rawSchemaJson).toString()
    assertEquals(rawSchemaJson, actual.parseJson())
  }

  @Test
  fun toStringTest() {
    val rawSchemaJson = readResourceAsJson("tostring/objectschema.json")
    val actual = JsonSchema.schemaReader().readSchema(rawSchemaJson).toString()
    assertEquals(rawSchemaJson, actual.parseJson())
  }

  @Test
  fun typeFailure() {
    failureOf(mockObjectSchema().build())
        .expectedKeyword("type")
        .input("a")
        .expect()
  }

  internal fun readResourceAsJson(url: String): kotlinx.serialization.json.JsonObject {
    return JsonSchema.resourceLoader().readJsonObject(url)
  }
}
