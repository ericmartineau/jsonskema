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
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.assertj.asserts.asserting
import io.mverse.jsonschema.assertj.asserts.hasKeyword
import io.mverse.jsonschema.assertj.asserts.hasSchemaLocation
import io.mverse.jsonschema.assertj.asserts.hasViolationAt
import io.mverse.jsonschema.assertj.asserts.isNotValid
import io.mverse.jsonschema.assertj.asserts.isValid
import io.mverse.jsonschema.assertj.asserts.validating
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.enums.JsonSchemaType.NULL
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.Keywords.Companion.ENUM
import io.mverse.jsonschema.keyword.Keywords.Companion.TYPE
import io.mverse.jsonschema.keyword.Keywords.Companion.UNIQUE_ITEMS
import io.mverse.jsonschema.loading.parseJson
import io.mverse.jsonschema.loading.parseJsonObject
import io.mverse.jsonschema.minus
import io.mverse.jsonschema.plus
import io.mverse.jsonschema.resourceLoader
import io.mverse.jsonschema.schemaBuilder
import io.mverse.jsonschema.schemaReader
import io.mverse.jsonschema.validation.ValidationMocks.mockArraySchema
import io.mverse.jsonschema.validation.ValidationMocks.mockBooleanSchema
import io.mverse.jsonschema.validation.ValidationMocks.mockNullSchema
import io.mverse.jsonschema.validation.ValidationMocks.mockNumberSchema
import io.mverse.jsonschema.validation.ValidationTestSupport.buildWithLocation
import io.mverse.jsonschema.validation.ValidationTestSupport.expectFailure
import io.mverse.jsonschema.validation.ValidationTestSupport.expectSuccess
import io.mverse.jsonschema.validation.ValidationTestSupport.failureOf
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.json
import lang.json.jsonArrayOf
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ArraySchemaValidatorTest {

  private var loader = JsonSchema.resourceLoader()
  private lateinit var arrayTestCases: JsonObject

  @Test
  fun additionalItemsSchema() {
    JsonSchema.schemaBuilder().itemSchema(mockBooleanSchema())
        .schemaOfAdditionalItems(mockNullSchema())
        .build()
        .asserting()
        .validating(arrayTestCases["additionalItemsSchema"])
        .isValid()
  }

  @Test
  fun additionalItemsSchemaFailure() {
    val nullSchema = JsonSchema.schemaBuilder("nulls").type(NULL)

    val subject = JsonSchema.schemaBuilder()
        .itemSchemas(listOf(mockBooleanSchema("#booleans")))
        .schemaOfAdditionalItems(nullSchema)
        .build()

    subject.asserting()
        .validating(arrayTestCases["additionalItemsSchemaFailure"])
        .hasViolationAt("#/2")
        .hasKeyword(Keywords.TYPE)
        .hasSchemaLocation("#/additionalItems")
  }

  @Before
  fun before() {
    arrayTestCases = JsonSchema.resourceLoader().readJsonObject("arraytestcases.json")
  }

  @Test
  fun booleanItems() {
    val subject = JsonSchema.schemaBuilder().allItemSchema(mockBooleanSchema()).build()
    assert(subject)
        .validating(arrayTestCases["boolArrFailure"])
        .isNotValid()
        .hasViolationAt("#/2")
  }

  @Test
  fun doesNotRequireExplicitArray() {
    val arraySchema = JsonSchema.schemaBuilder()
        .needsUniqueItems(true)
        .build()
    assert(arraySchema)
        .validating(arrayTestCases["doesNotRequireExplicitArray"])
        .isValid()
  }

  @Test
  fun maxItems() {
    val subject = buildWithLocation(JsonSchema.schemaBuilder().maxItems(0))
    failureOf(subject)
        .schema(subject)
        .expectedPointer("#")
        .expectedKeyword("maxItems")
        .expectedMessageFragment("expected maximum item count: 0, found: 1")
        .input(arrayTestCases.get("onlyOneItem"))
        .expect()
  }

  @Test
  fun minItems() {
    val subject = buildWithLocation(JsonSchema.schemaBuilder().minItems(2))
    failureOf(subject)
        .expectedPointer("#")
        .expectedKeyword("minItems")
        .input(arrayTestCases["onlyOneItem"])
        .expect()
  }

  @Test
  fun noItemSchema() {
    val schema = JsonSchema.schemaBuilder().build()
    expectSuccess(schema, arrayTestCases!!.get("noItemSchema"))
  }

  @Test
  fun nonUniqueArrayOfArrays() {
    val subject = buildWithLocation(JsonSchema.schemaBuilder().needsUniqueItems(true))
    failureOf(subject)
        .expectedPointer("#")
        .expectedKeyword("uniqueItems")
        .input(arrayTestCases!!.get("nonUniqueArrayOfArrays"))
        .expect()
  }

  @Test
  fun toStringAdditionalItems() {
    val addtlProps = json { "type" to "boolean" }
    val rawSchemaJson = loader
        .readJsonObject("tostring/arrayschema-list.json")
        .minus("items")
        .plus("additionalItems" to addtlProps)
    val actual = JsonSchema.schemaReader().readSchema(rawSchemaJson).toString()
    assertEquals(addtlProps, actual.parseJsonObject()["additionalItems"])
  }

  @Test
  fun toStringNoExplicitType() {
    val rawSchemaJson = loader.readJsonObject("tostring/arrayschema-list.json") - "type"
    val serializedSchema = JsonSchema.schemaReader().readSchema(rawSchemaJson).toString()
    assertEquals(rawSchemaJson, serializedSchema.parseJsonObject())
  }

  @Test
  fun toStringTest() {
    val rawSchemaJson = loader.readJsonObject("tostring/arrayschema-list.json")
    val serializedSchema = JsonSchema.schemaReader().readSchema(rawSchemaJson).toString()
    assertEquals(rawSchemaJson, serializedSchema.parseJsonObject())
  }

  @Test
  fun toStringTupleSchema() {
    val rawSchemaJson = loader.readJsonObject("tostring/arrayschema-tuple.json")
    val serializaedSchema = JsonSchema.schemaReader().readSchema(rawSchemaJson).toString()
    assertEquals(rawSchemaJson, serializaedSchema.parseJsonObject())
  }

  @Test
  fun tupleWithOneItem() {
    // if (itemSchemas == null) {
    //     itemSchemas = new ArrayList<>();
    // }
    // itemSchemas.add(requireNonNull(itemSchema, "itemSchema cannot be null"));
    // return this;
    val subject = JsonSchema.schemaBuilder().itemSchema(mockBooleanSchema()).build().asDraft6()
    val expectedSchema = subject.itemSchemas[0]

    failureOf(subject)
        .expectedViolatedSchema(expectedSchema)
        .expectedSchemaLocation("#/items/0")
        .expectedPointer("#/0")
        .input(arrayTestCases["tupleWithOneItem"])
        .expect()
  }

  @Test
  fun typeFailure() {
    failureOf(mockArraySchema().build())
        .expectedKeyword("type")
        .input(true)
        .expect()
  }

  @Test
  fun uniqueItemsObjectViolation() {
    val subject = JsonSchema.schemaBuilder().needsUniqueItems(true).build()
    expectFailure(subject, "#", arrayTestCases!!.get("nonUniqueObjects"))
  }

  @Test
  fun uniqueItemsViolation() {
    val subject = JsonSchema.schemaBuilder().needsUniqueItems(true).build()
    expectFailure(subject, "#", arrayTestCases!!.get("nonUniqueItems"))
  }

  @Test
  fun uniqueItemsWithSameToString() {
    val schema = JsonSchema.schemaBuilder().needsUniqueItems(true).build()
    expectSuccess(schema, arrayTestCases!!.get("uniqueItemsWithSameToString"))
  }

  @Test
  fun uniqueObjectValues() {
    val schema = JsonSchema.schemaBuilder().needsUniqueItems(true).build()
    expectSuccess(schema, arrayTestCases!!.get("uniqueObjectValues"))
  }

  @Test
  fun validate_WhenEqualNumbersWithDifferentLexicalRepresentations_ThenUnique() {
    val arraySchema = mockArraySchema().needsUniqueItems(true).build()
    arraySchema.validating("[1.0, 1, 1.00]".parseJson())
        .isValid()
  }

  @Test
  fun validate_WhenEqualNumbersWithSameLexicalRepresentations_ThenNotUnique() {
    val arraySchema = mockArraySchema().needsUniqueItems(true).build()
    val subject = "[1.0, 1.0, 1.00]".parseJson().jsonArray
    arraySchema.validating(subject)
        .isNotValid()
        .hasKeyword(UNIQUE_ITEMS)
  }

  @Test
  fun validate_WhenItemsSchemaHasEnum_AndArrayValueIsInEnumButWrongType_ThenFailWithTypeKeyword() {
    val enumSchema = ValidationMocks.mockIntegerSchema()
        .enumValues(jsonArrayOf(12, 24.3, 65))

    val arraySchema = JsonSchema.schemaBuilder()
        .allItemSchema(enumSchema)
        .build()

    val arrayValues = jsonArrayOf(24.3)
    val error = ValidationMocks.createTestValidator(arraySchema).validate(arrayValues)

    assert(error).isNotNull()
    assert(error!!.keyword).isEqualTo(TYPE)
    assert(error.arguments).isNotNull {
      it.containsExactly(JsonSchemaType.INTEGER, JsonSchemaType.NUMBER)
    }
  }

  @Test
  fun validate_WhenItemsSchemaHasEnum_ThenDontEnforceLexicalMatching() {

    val enumSchema = mockNumberSchema()
        .enumValues("[12, 24.3, 65]".parseJson().jsonArray)

    val arraySchema = JsonSchema.schemaBuilder()
        .allItemSchema(enumSchema)
        .build()

    val arrayValues = "[24.3, 12]".parseJson().jsonArray
    arraySchema.validating(arrayValues)
        .isValid()
  }

  @Test
  fun validate_WhenItemsSchemaHasEnum_ThenEnforceEachItem() {
    val enumSchema = mockNumberSchema()
        .enumValues(jsonArrayOf(12, 24.3, 65))

    val arraySchema = JsonSchema.schemaBuilder()
        .allItemSchema(enumSchema)
        .build()

    val arrayValues = jsonArrayOf(24.30, 13)
    arraySchema.validating(arrayValues)
        .isNotValid()
        .hasViolationAt("#/1")
        .hasKeyword(ENUM)
        .hasSchemaLocation("#/items")
  }

  private fun ValidationMocks.createTestValidator(schema: Schema): SchemaValidator {
    return SchemaValidatorFactoryImpl.builder().build().createValidator(schema)
  }
}
