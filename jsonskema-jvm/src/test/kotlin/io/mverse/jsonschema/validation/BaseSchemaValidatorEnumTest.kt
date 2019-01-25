package io.mverse.jsonschema.validation

import assertk.assert
import assertk.assertAll
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.JsonSchema.schemaBuilder
import io.mverse.jsonschema.assertj.asserts.isValid
import io.mverse.jsonschema.assertj.asserts.validating
import io.mverse.jsonschema.builder.MutableSchema
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.loading.parseJsrJson
import io.mverse.jsonschema.loading.parseJsrObject
import io.mverse.jsonschema.validation.ValidationMocks.mockSchema
import io.mverse.jsonschema.validation.ValidationTestSupport.expectSuccess
import io.mverse.jsonschema.validation.ValidationTestSupport.failureOf
import io.mverse.jsonschema.validation.ValidationTestSupport.verifyFailure
import kotlinx.serialization.json.JsonArray
import lang.json.JsrArray
import lang.json.JsrObject
import lang.json.createJsrArray
import lang.json.get
import lang.json.jkey
import lang.json.jsrArrayOf
import lang.json.jsrObject
import lang.json.mutate
import lang.json.toJsrValue
import lang.json.toKtArray
import lang.json.toMutableJsonArray
import org.junit.Before
import org.junit.Test

class BaseSchemaValidatorEnumTest {
  private lateinit var possibleValues: JsrArray

  @Before
  fun before() {
    possibleValues = jsrArrayOf(true, "foo")
  }

  @Test
  fun failure() {
    failureOf(subjectBuilder())
        .expectedPointer("#")
        .expectedKeyword(Keywords.ENUM)
        .input(jsrArrayOf(1))
        .expect()
  }

  @Test
  fun objectInArrayMatches() {
    val possibleValues = createJsrArray(this.possibleValues + jsrObject { "a" to true })
    val subject = subjectBuilder().build { enumValues = possibleValues }

    val testValues = jsrObject { "a" to true }
    subject.validating(testValues)
        .isValid()
  }

  @Test
  fun success() {
    val validJsonObject = jsrObject {
      "a" *= 0
    }
    possibleValues = possibleValues.mutate {
      plus(jsrArrayOf())
      plus(validJsonObject)
    }
    val schema = subjectBuilder().build()
    val subject = ValidationMocks.createTestValidator(schema)

    assertAll {
      expectSuccess { subject.validate(true.toJsrValue()) }
      expectSuccess { subject.validate("foo".toJsrValue()) }
      expectSuccess { subject.validate(jsrArrayOf()) }
      expectSuccess { subject.validate(validJsonObject) }
    }
  }

  @Test
  fun toStringTest() {
    val toString = subjectBuilder().build().toString()
    val actual = toString.parseJsrObject()
    assert(actual).hasSize(1)
    val pv = jsrArrayOf(true, "foo")
    assert(actual["enum".jkey]).isEqualTo(pv)
  }

  @Test
  fun validate_WhenNumbersHaveDifferentLexicalValues_EnumDoesntMatch() {
    val testEnum = "[1, 1.0, 1.00]".parseJsrJson().asJsonArray()
    val testValNotSame = toJsrValue("1.000")

    val schema = JsonSchema.schema { enumValues = testEnum }

    val validate = ValidationMocks.createTestValidator(schema).validate(testValNotSame)

    assert(validate, "Should have an error").isNotNull()
    assert(validate!!.keyword, "Should be for enum keyword").isEqualTo(Keywords.ENUM)
  }

  @Test
  fun validate_WhenNumbersHaveSameLexicalValues_EnumMatches() {
    val testEnum = "[1, 1.0, 1.00]".parseJsrJson().asJsonArray()
    val testValNotSame = "1.00".parseJsrJson()

    val schema = mockSchema.build { enumValues = testEnum }
    schema.validating(testValNotSame)
        .isValid()
  }

  @Test
  fun validate_WhenSubjectIsArray_AndEnumIsAppliedToTheArray_AndArrayMatchesItemsInOrder_ThenTheArrayValidates() {
    // To validate you either need to be:
    // An array with items [true, "foo", {"a": true}], OR
    // The number literal 42
    val possibleValuesContainer = jsrArrayOf(possibleValues.plus(jsrObject { "a" to true }), 42)
    val subject = subjectBuilder().build { enumValues = possibleValuesContainer }
    val testValues = jsrArrayOf(true, "foo", jsrObject { "a" to true })

    expectSuccess {
      val error = ValidationMocks.createTestValidator(subject).validate(testValues)
      error
    }
  }

  @Test
  fun validate_WhenSubjectIsArray_AndEnumIsAppliedToTheArray_ThenTheArrayFailsToValidate() {
    val possibleValues = possibleValues.toMutableJsonArray() + jsrObject { "a" *= true }

    val subject = subjectBuilder().build { enumValues = possibleValues.build() }

    val testValues = jsrArrayOf(jsrObject { "a" to true })

    verifyFailure {
      ValidationMocks.createTestValidator(subject).validate(testValues)
    }
  }

  private fun subjectBuilder(): MutableSchema {
    return schemaBuilder { enumValues = possibleValues }
  }

  operator fun JsonArray.plus(iterable: Iterable<Any?>): JsonArray = (this.content + iterable).toKtArray()
  operator fun JsonArray.plus(element: JsrObject): JsonArray = (this.content + element).toKtArray()
}
