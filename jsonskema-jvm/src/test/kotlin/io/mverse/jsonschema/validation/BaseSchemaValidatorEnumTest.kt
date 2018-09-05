package io.mverse.jsonschema.validation

import assertk.assert
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.SchemaBuilder
import io.mverse.jsonschema.assertj.asserts.isValid
import io.mverse.jsonschema.assertj.asserts.validating
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.loading.parseJson
import io.mverse.jsonschema.loading.parseJsonObject
import io.mverse.jsonschema.schemaBuilder
import io.mverse.jsonschema.validation.ValidationMocks.createTestValidator
import io.mverse.jsonschema.validation.ValidationMocks.mockSchema
import io.mverse.jsonschema.validation.ValidationTestSupport.expectSuccess
import io.mverse.jsonschema.validation.ValidationTestSupport.failureOf
import io.mverse.jsonschema.validation.ValidationTestSupport.verifyFailure
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.json
import lang.json.jsonArrayOf
import lang.json.toJsonLiteral
import lang.json.toJsonArray
import org.junit.Before
import org.junit.Test

class BaseSchemaValidatorEnumTest {
  private lateinit var possibleValues: kotlinx.serialization.json.JsonArray

  @Before
  fun before() {
    possibleValues = listOf(true, "foo").toJsonArray()
  }

  @Test
  fun failure() {
    failureOf(subjectBuilder())
        .expectedPointer("#")
        .expectedKeyword(Keywords.ENUM)
        .input(jsonArrayOf(1))
        .expect()
  }

  @Test
  fun objectInArrayMatches() {
    val possibleValues = (this.possibleValues + json { "a" to true }).toJsonArray()
    val subject = subjectBuilder().enumValues(possibleValues).build()

    val testValues = json { "a" to true }
    subject.validating(testValues)
        .isValid()
  }

  @Test
  fun success() {
    val validJsonObject = json {
      "a" to 0
    }
    possibleValues += jsonArrayOf(listOf<Any>(), validJsonObject)
    val schema = subjectBuilder().build()
    val subject = ValidationMocks.createTestValidator(schema)

    expectSuccess { subject.validate(true.toJsonLiteral()) }
    expectSuccess { subject.validate("foo".toJsonLiteral()) }
    expectSuccess { subject.validate(jsonArrayOf()) }
    expectSuccess { subject.validate(validJsonObject) }
  }

  @Test
  fun toStringTest() {
    val toString = subjectBuilder().build().toString()
    val actual = toString.parseJsonObject()
    assert(actual).hasSize(1)
    val pv = jsonArrayOf(true, "foo")
    assert(actual["enum"]).isEqualTo(pv)
  }

  @Test
  fun validate_WhenNumbersHaveDifferentLexicalValues_EnumDoesntMatch() {
    val testEnum = "[1, 1.0, 1.00]".parseJson().jsonArray
    val testValNotSame = "1.000".toJsonLiteral()

    val schema = JsonSchema.schemaBuilder().enumValues(testEnum).build()

    val validate = ValidationMocks.createTestValidator(schema).validate(testValNotSame)

    assert(validate, "Should have an error").isNotNull()
    assert(validate!!.keyword, "Should be for enum keyword").isEqualTo(Keywords.ENUM)
  }

  @Test
  fun validate_WhenNumbersHaveSameLexicalValues_EnumMatches() {
    val testEnum = "[1, 1.0, 1.00]".parseJson().jsonArray
    val testValNotSame = "1.00".parseJson()

    val schema = mockSchema().enumValues(testEnum).build()
    schema.validating(testValNotSame)
        .isValid()
  }

  @Test
  fun validate_WhenSubjectIsArray_AndEnumIsAppliedToTheArray_AndArrayMatchesItemsInOrder_ThenTheArrayValidates() {
    // To validate you either need to be:
    // An array with items [true, "foo", {"a": true}], OR
    // The number literal 42
    val possibleValuesContainer = jsonArrayOf(
        possibleValues + json { "a" to true },
        42)

    val subject = subjectBuilder().enumValues(possibleValuesContainer).build()
    val testValues = jsonArrayOf(true, "foo", json { "a" to true })

    expectSuccess {
      val error = ValidationMocks.createTestValidator(subject).validate(testValues)
      error
    }
  }

  @Test
  fun validate_WhenSubjectIsArray_AndEnumIsAppliedToTheArray_ThenTheArrayFailsToValidate() {
    val possibleValues = this.possibleValues + json { "a" to true }

    val subject = subjectBuilder().enumValues(possibleValues).build()

    val testValues = jsonArrayOf(json { "a" to true })

    verifyFailure {
      ValidationMocks.createTestValidator(subject).validate(testValues)
    }
  }

  private fun subjectBuilder(): SchemaBuilder {
    return JsonSchema.schemaBuilder().enumValues(possibleValues)
  }

  operator fun kotlinx.serialization.json.JsonArray.plus(iterable: Iterable<Any?>): kotlinx.serialization.json.JsonArray = (this.content + iterable).toJsonArray()
  operator fun kotlinx.serialization.json.JsonArray.plus(element: kotlinx.serialization.json.JsonObject): kotlinx.serialization.json.JsonArray = (this.content + element).toJsonArray()
}
