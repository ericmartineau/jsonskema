package io.mverse.jsonschema.validation.keywords

import assertk.assert
import io.mverse.jsonschema.Draft7Schema
import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.assertj.asserts.hasErrorArguments
import io.mverse.jsonschema.assertj.asserts.hasErrorCode
import io.mverse.jsonschema.assertj.asserts.hasViolationAt
import io.mverse.jsonschema.assertj.asserts.isNotValid
import io.mverse.jsonschema.assertj.asserts.isValid
import io.mverse.jsonschema.assertj.asserts.validating
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.resourceLoader
import io.mverse.jsonschema.schemaReader
import kotlinx.serialization.json.json
import org.junit.Test

class LogicValidatorTest {

  @Test
  fun testLogic_WhenStartsWithQ_ThenMaxLengthIs10() {
    val jsonObject = loader.readJsonObject("/draft7-keywords.json")
    val schema = JsonSchema.schemaReader().readSchema(jsonObject).asDraft7()

    assert(schema)
        .validating(json { "ifTest" to "quadrilateralus" })
        .isNotValid {
          hasViolationAt("#/ifTest")
              .hasErrorCode("validation.keyword.maxLength")
              .hasErrorArguments(10, 15)
        }
  }

  fun kotlinx.serialization.json.JsonObject.toJsonSchema(): Draft7Schema =
      JsonSchema.schemaReader().readSchema(this).asDraft7()

  @Test
  fun testLogic_WhenStartsWithQ_ThenMaxLengthIs10_Success() {
    val schema = loader.readJsonObject("/draft7-keywords.json")
        .toJsonSchema()

    assert(schema)
        .validating(json { "ifTest" to "quadruple" })
        .isValid()
  }

  @Test
  fun testLogic_WhenStartsWithS_ThenMaxLengthIs5_Failure() {
    val schema = loader.readJsonObject("/draft7-keywords.json").toJsonSchema().asDraft7()

    assert(schema)
        .validating(json { "ifTest" to "smasher" })
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
        .validating(json { "ifTest" to "smash" })
        .isValid()
  }

  @Test
  fun testLogic_WhenNotAString_ThenMustBeObject_Failure() {
    val schema = loader.readJsonObject("/draft7-keywords.json").toJsonSchema()

    assert(schema)
        .validating(json { "ifTest" to 10.0 })
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
        .validating(json { "ifTest" to json {} })
        .isValid()
  }

  companion object {
    private val loader = JsonSchema.resourceLoader()
  }
}
