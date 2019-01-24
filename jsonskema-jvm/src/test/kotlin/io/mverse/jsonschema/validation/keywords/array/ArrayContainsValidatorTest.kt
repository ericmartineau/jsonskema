package io.mverse.jsonschema.validation.keywords.array

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.assertj.asserts.isValid
import io.mverse.jsonschema.assertj.asserts.validating
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.validation.ValidationMocks
import lang.json.jsrArrayOf
import lang.json.jsrJson
import lang.json.jsrNumber
import lang.json.jsrString
import lang.json.toJsrValue
import lang.json.toKtArray
import org.junit.Test

class ArrayContainsValidatorTest {
  @Test
  fun validate_DoesntContains() {
    val containsSchema = JsonSchema.schema {
      type = JsonSchemaType.ARRAY
      containsSchema = JsonSchema.schemaBuilder {
        anyOfSchemas = listOf(
            schemaBuilder { constValue = jsrNumber(3) },
            schemaBuilder { constValue = jsrNumber(4.0) },
            schemaBuilder { constValue = jsrString("5") })
      }
    }

    val testValidator = ValidationMocks.createTestValidator(containsSchema)
    val invalidArray = jsrArrayOf(24, "Bob", 5)

    val validate = testValidator.validate(invalidArray)
    assert(validate).isNotNull()
    val error = validate!!
    assert(error.keyword).isEqualTo(Keywords.CONTAINS)
  }

  @Test
  fun validate_Contains() {
    jsrJson {
      val containsSchema = JsonSchema.schema {
        type = JsonSchemaType.ARRAY
        containsSchema = JsonSchema.schemaBuilder().apply {
          anyOfSchemas = listOf(
              schemaBuilder { constValue = 3.toJsrJson() },
              schemaBuilder { constValue = 4.0.toJsrJson() },
              schemaBuilder { constValue = "5".toJsrJson() })
        }
      }

      containsSchema
          .validating(jsrArrayOf(24, "Bob", 5, 3))
          .isValid()
    }
  }
}
