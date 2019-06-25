package io.mverse.jsonschema.validation.keywords.array

import assertk.assert
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import io.mverse.jsonschema.JsonSchemas
import io.mverse.jsonschema.assertj.asserts.isValid
import io.mverse.jsonschema.assertj.asserts.validating
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.schema
import io.mverse.jsonschema.validation.ValidationMocks
import lang.json.jsrArrayOf
import lang.json.jsrNumber
import lang.json.jsrString
import lang.json.toJsrValue
import org.junit.Test

class ArrayContainsValidatorTest {
  @Test
  fun validate_DoesntContains() {
    val containsSchema = schema {
      type = JsonSchemaType.ARRAY
      containsSchema {
        anyOfSchema { constValue = jsrNumber(3) }
        anyOfSchema { constValue = jsrNumber(4.0) }
        anyOfSchema { constValue = jsrString("5") }
      }
    }

    val testValidator = ValidationMocks.createTestValidator(containsSchema)
    val invalidArray = jsrArrayOf(24, "Bob", 5)

    val validate = testValidator.validate(invalidArray)
    assertThat(validate).isNotNull()
    val error = validate!!
    assertThat(error.keyword).isEqualTo(Keywords.CONTAINS)
  }

  @Test
  fun validate_Contains() {

    val containsSchema = JsonSchemas.schema {
      type = JsonSchemaType.ARRAY
      containsSchema {
        anyOfSchema { constValue = 3.toJsrValue() }
        anyOfSchema { constValue = 4.0.toJsrValue() }
        anyOfSchema { constValue = "5".toJsrValue() }
      }
    }

    containsSchema
        .validating(jsrArrayOf(24, "Bob", 5, 3))
        .isValid()
  }
}
