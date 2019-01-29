package io.mverse.jsonschema.validation.keywords

import assertk.assert
import io.mverse.jsonschema.JsonSchemas
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.assertj.asserts.hasErrorArguments
import io.mverse.jsonschema.assertj.asserts.hasErrorCode
import io.mverse.jsonschema.assertj.asserts.hasViolationAt
import io.mverse.jsonschema.assertj.asserts.isNotValid
import io.mverse.jsonschema.assertj.asserts.isValid
import io.mverse.jsonschema.assertj.asserts.validating
import io.mverse.jsonschema.createSchemaReader
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.resourceLoader
import lang.json.jsrObject
import org.junit.Test

class LogicValidatorTest {

  @Test
  fun testLogic_WhenStartsWithQ_ThenMaxLengthIs10() {
    val jsonObject = loader.readJsonObject("/draft7-keywords.json")
    val schema = JsonSchemas.createSchemaReader().readSchema(jsonObject)

    assert(schema)
        .validating(jsrObject { "ifTest" *= "quadrilateralus" })
        .isNotValid {
          hasViolationAt("#/ifTest")
              .hasErrorCode("validation.keyword.maxLength")
              .hasErrorArguments(10, 15)
        }
  }

  fun lang.json.JsrObject.toJsonSchema(): Schema =
      JsonSchemas.createSchemaReader().readSchema(this)

  @Test
  fun testLogic_WhenStartsWithQ_ThenMaxLengthIs10_Success() {
    val schema = loader.readJsonObject("/draft7-keywords.json")
        .toJsonSchema()

    assert(schema)
        .validating(jsrObject { "ifTest" *= "quadruple" })
        .isValid()
  }

  @Test
  fun testLogic_WhenStartsWithS_ThenMaxLengthIs5_Failure() {
    val schema = loader.readJsonObject("/draft7-keywords.json").toJsonSchema()

    assert(schema)
        .validating(jsrObject { "ifTest" *= "smasher" })
        .isNotValid {
          hasViolationAt("#/ifTest")
              .hasErrorCode("validation.keyword.maxLength")
              .hasErrorArguments(5, 7)
        }
  }

  @Test
  fun testLogic_WhenStartsWithS_ThenMaxLengthIs5_Success() {
    val schema = loader.readJsonObject("/draft7-keywords.json").toJsonSchema()

    assert(schema)
        .validating(jsrObject { "ifTest" *= "smash" })
        .isValid()
  }

  @Test
  fun testLogic_WhenNotAString_ThenMustBeObject_Failure() {
    val schema = loader.readJsonObject("/draft7-keywords.json").toJsonSchema()

    assert(schema)
        .validating(jsrObject { "ifTest" *= 10.0 })
        .isNotValid {
          hasViolationAt("#/ifTest")
              .hasErrorCode("validation.typeMismatch")
              .hasErrorArguments(JsonSchemaType.OBJECT, JsonSchemaType.NUMBER)
        }
  }

  @Test
  fun testLogic_WhenNotAString_ThenMustBeObject_Success() {
    val schema = loader.readJsonObject("/draft7-keywords.json").toJsonSchema()

    assert(schema)
        .validating(jsrObject { "ifTest" *= jsrObject {} })
        .isValid()
  }

  companion object {
    private val loader = JsonSchemas.resourceLoader()
  }
}
