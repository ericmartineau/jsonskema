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
import io.mverse.jsonschema.JsonSchemas
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
import io.mverse.jsonschema.keyword.Keywords.ENUM
import io.mverse.jsonschema.keyword.Keywords.TYPE
import io.mverse.jsonschema.keyword.Keywords.UNIQUE_ITEMS
import io.mverse.jsonschema.loading.parseJsrJson
import io.mverse.jsonschema.loading.parseJsrObject
import io.mverse.jsonschema.resourceLoader
import io.mverse.jsonschema.schema
import io.mverse.jsonschema.schemaBuilder
import io.mverse.jsonschema.validation.ValidationMocks.mockArraySchema
import io.mverse.jsonschema.validation.ValidationMocks.mockBooleanSchema
import io.mverse.jsonschema.validation.ValidationMocks.mockNullSchema
import io.mverse.jsonschema.validation.ValidationMocks.mockNumberSchema
import io.mverse.jsonschema.validation.ValidationTestSupport.buildWithLocation
import io.mverse.jsonschema.validation.ValidationTestSupport.expectFailure
import io.mverse.jsonschema.validation.ValidationTestSupport.expectSuccess
import io.mverse.jsonschema.validation.ValidationTestSupport.failureOf
import io.mverse.logging.mlogger
import lang.json.JsonKey
import lang.json.JsrArray
import lang.json.JsrObject
import lang.json.get
import lang.json.jkey
import lang.json.jsrArrayOf
import lang.json.jsrJson
import lang.json.jsrObject
import lang.json.mutate
import lang.json.toJsrObject
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ArraySchemaValidatorTest {

  private var loader = JsonSchemas.resourceLoader()
  private lateinit var arrayTestCases: JsrObject

  @Test
  fun additionalItemsSchema() {
    JsonSchemas.schema {
      itemSchemas = listOf(mockBooleanSchema)
      schemaOfAdditionalItems = mockNullSchema
    }
        .asserting()
        .validating(arrayTestCases[JsonKey("additionalItemsSchema")])
        .isValid()
  }

  @Test
  fun additionalItemsSchemaFailure() {
    val subject = JsonSchemas.schema {
      itemSchema { type = JsonSchemaType.BOOLEAN }
      schemaOfAdditionalItems { type = NULL }
    }

    val validatee = arrayTestCases["additionalItemsSchemaFailure"]
    log.info { "Validating $validatee" }
    subject.asserting()
        .validating(validatee)
        .hasViolationAt("#/2")
        .hasKeyword(Keywords.TYPE)
        .hasSchemaLocation("#/additionalItems")
  }

  @Before
  fun before() {
    arrayTestCases = JsonSchemas.resourceLoader().readJsonObject("arraytestcases.json")
  }

  @Test
  fun booleanItems() {
    val subject = JsonSchemas.schema { allItemSchema = mockBooleanSchema }
    assert(subject)
        .validating(arrayTestCases["boolArrFailure"])
        .isNotValid()
        .hasViolationAt("#/2")
  }

  @Test
  fun doesNotRequireExplicitArray() {
    val arraySchema = JsonSchemas.schema {
      needsUniqueItems = true
    }
    assert(arraySchema)
        .validating(arrayTestCases["doesNotRequireExplicitArray"])
        .isValid()
  }

  @Test
  fun maxItems() {
    val subject = buildWithLocation(schemaBuilder { maxItems = 0 })
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
    val subject = buildWithLocation(schemaBuilder { minItems = 2 })
    failureOf(subject)
        .expectedPointer("#")
        .expectedKeyword("minItems")
        .input(arrayTestCases["onlyOneItem"])
        .expect()
  }

  @Test
  fun noItemSchema() {
    val schema = schema {}
    expectSuccess(schema, arrayTestCases["noItemSchema".jkey])
  }

  @Test
  fun nonUniqueArrayOfArrays() {
    val subject = buildWithLocation(schemaBuilder { needsUniqueItems = true })
    failureOf(subject)
        .expectedPointer("#")
        .expectedKeyword("uniqueItems")
        .input(arrayTestCases.get("nonUniqueArrayOfArrays"))
        .expect()
  }

  @Test
  fun toStringAdditionalItems() {
    jsrJson {
      val addtlProps = jsrObject { "type" *= "boolean" }
      val rawSchemaJson = loader
          .readJsonObject("tostring/arrayschema-list.json")
          .mutate {
            removePath("items")
            add("additionalItems", addtlProps)
          }

      val actual = JsonSchemas.schemaReader.readSchema(rawSchemaJson).toString()
      assertEquals(addtlProps, actual.parseJsrObject()["additionalItems"])
    }
  }

  @Test
  fun toStringNoExplicitType() {
    jsrJson {

      val rawSchemaJson = loader.readJsonObject("tostring/arrayschema-list.json") - "type"
      val serializedSchema = JsonSchemas.schemaReader.readSchema(rawSchemaJson.toJsrObject()).toString()
      assertEquals(rawSchemaJson, serializedSchema.parseJsrObject())
    }
  }

  @Test
  fun toStringTest() {
    val rawSchemaJson = loader.readJsonObject("tostring/arrayschema-list.json")
    val serializedSchema = JsonSchemas.schemaReader.readSchema(rawSchemaJson).toString()
    assertEquals(rawSchemaJson, serializedSchema.parseJsrObject())
  }

  @Test
  fun toStringTupleSchema() {
    val rawSchemaJson = loader.readJsonObject("tostring/arrayschema-tuple.json")
    val serializaedSchema = JsonSchemas.schemaReader.readSchema(rawSchemaJson).toString()
    assertEquals(rawSchemaJson, serializaedSchema.parseJsrObject())
  }

  @Test
  fun tupleWithOneItem() {
    // if (itemSchemas == null) {
    //     itemSchemas = new ArrayList<>();
    // }
    // itemSchemas.add(requireNonNull(itemSchema, "itemSchema cannot be null"));
    // return this;
    val subject = JsonSchemas.schema { itemSchemas = listOf(mockBooleanSchema) }
    val expectedSchema = subject.asDraft6().itemSchemas[0]

    failureOf(subject)
        .expectedViolatedSchema(expectedSchema)
        .expectedSchemaLocation("#/items/0")
        .expectedPointer("#/0")
        .input(arrayTestCases["tupleWithOneItem"])
        .expect()
  }

  @Test
  fun typeFailure() {
    failureOf(mockArraySchema {})
        .expectedKeyword("type")
        .input(true)
        .expect()
  }

  @Test
  fun uniqueItemsObjectViolation() {
    val subject = schema { needsUniqueItems = true }
    expectFailure(subject, "#", arrayTestCases.get("nonUniqueObjects".jkey))
  }

  @Test
  fun uniqueItemsViolation() {
    val subject = JsonSchemas.schema { needsUniqueItems = true }
    expectFailure(subject, "#", arrayTestCases.get("nonUniqueItems".jkey))
  }

  @Test
  fun uniqueItemsWithSameToString() {
    val schema = JsonSchemas.schema { needsUniqueItems = true }
    expectSuccess(schema, arrayTestCases.get("uniqueItemsWithSameToString".jkey))
  }

  @Test
  fun uniqueObjectValues() {
    val schema = JsonSchemas.schema { needsUniqueItems = true }
    expectSuccess(schema, arrayTestCases.get("uniqueObjectValues".jkey))
  }

  @Test
  fun validate_WhenEqualNumbersWithDifferentLexicalRepresentations_ThenUnique() {
    val arraySchema = mockArraySchema.build { needsUniqueItems = true }
    arraySchema.validating("[1.0, 1, 1.00]".parseJsrJson())
        .isValid()
  }

  @Test
  fun validate_WhenEqualNumbersWithSameLexicalRepresentations_ThenNotUnique() {
    val arraySchema = mockArraySchema.build { needsUniqueItems = true }
    val subject = "[1.0, 1.0, 1.00]".parseJsrJson().asJsonArray()
    arraySchema.validating(subject)
        .isNotValid()
        .hasKeyword(UNIQUE_ITEMS)
  }

  @Test
  fun validate_WhenItemsSchemaHasEnum_AndArrayValueIsInEnumButWrongType_ThenFailWithTypeKeyword() {
    val enumSchema = ValidationMocks.mockIntegerSchema.apply {
      enumValues = jsrArrayOf(12, 24.3, 65)
    }

    val arraySchema = schema {
      allItemSchema = enumSchema
    }

    val arrayValues = jsrArrayOf(24.3)
    val error = ValidationMocks.createTestValidator(arraySchema).validate(arrayValues)

    assert(error).isNotNull()
    assert(error!!.keyword).isEqualTo(TYPE)
    assert(error.arguments).isNotNull {
      it.containsExactly(JsonSchemaType.INTEGER, JsonSchemaType.NUMBER)
    }
  }

  @Test
  fun validate_WhenItemsSchemaHasEnum_ThenDontEnforceLexicalMatching() {

    val enumSchema = mockNumberSchema.apply {
      enumValues = ("[12, 24.3, 65]".parseJsrJson() as JsrArray)
    }

    val arraySchema = schema {
      allItemSchema = enumSchema
    }

    val arrayValues = ("[24.3, 12]".parseJsrJson() as JsrArray)
    arraySchema.validating(arrayValues)
        .isValid()
  }

  @Test
  fun validate_WhenItemsSchemaHasEnum_ThenEnforceEachItem() {
    val enumSchema = mockNumberSchema.apply {
      enumValues = jsrArrayOf(12, 24.3, 65)
    }

    val arraySchema = schema {
      allItemSchema = enumSchema
    }

    val arrayValues = jsrArrayOf(24.30, 13)
    arraySchema.validating(arrayValues)
        .isNotValid()
        .hasViolationAt("#/1")
        .hasKeyword(ENUM)
        .hasSchemaLocation("#/items")
  }

  companion object {
    val log = mlogger {}
  }
}
