package io.mverse.jsonschema.validation.keywords

import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.assertj.asserts.assertValidation
import io.mverse.jsonschema.assertj.asserts.hasErrorArguments
import io.mverse.jsonschema.assertj.asserts.hasErrorCode
import io.mverse.jsonschema.assertj.asserts.hasKeyword
import io.mverse.jsonschema.assertj.asserts.hasViolationAt
import io.mverse.jsonschema.assertj.asserts.isNotValid
import io.mverse.jsonschema.assertj.asserts.isValid
import io.mverse.jsonschema.assertj.asserts.validating
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.keyword.Keywords.ADDITIONAL_PROPERTIES
import kotlinx.serialization.json.json
import lang.json.jsrObject
import kotlin.test.Test

class AdditionalPropertiesValidatorTest {
  @Test
  fun testValidationFailureMessage() {
    val nameOnly = JsonSchema.schema {
      type = JsonSchemaType.OBJECT
      properties["name"] = {
        type = JsonSchemaType.STRING
      }
      additionalProperties = false
    }

    val goodJson = jsrObject {
      "name" *= "John Doe"
    }

    val badJson = jsrObject {
      "named" *= "John Doe"
      "bogus" *= "Bardage"
    }

    nameOnly.validating(goodJson)
        .isValid()

    nameOnly.validating(badJson)
        .isNotValid()
        .assertValidation {

          hasViolationAt("#/named")
              .hasKeyword(ADDITIONAL_PROPERTIES)
              .hasErrorArguments("/named")
              .hasErrorCode("validation.keyword.additionalProperties")

          hasViolationAt("#/bogus")
              .hasKeyword(ADDITIONAL_PROPERTIES)
              .hasErrorArguments("/bogus")
              .hasErrorCode("validation.keyword.additionalProperties")
        }
  }
}
