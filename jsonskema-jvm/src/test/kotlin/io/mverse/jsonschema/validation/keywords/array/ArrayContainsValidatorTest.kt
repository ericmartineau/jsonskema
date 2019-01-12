package io.mverse.jsonschema.validation.keywords.array

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.assertj.asserts.isValid
import io.mverse.jsonschema.assertj.asserts.validating
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.schemaBuilder
import io.mverse.jsonschema.validation.ValidationMocks
import lang.json.jsonArrayOf
import lang.json.toKtArray
import lang.json.toJsonLiteral
import org.junit.Test

class ArrayContainsValidatorTest {
  @Test
  fun validate_DoesntContains() {
    val containsSchema = JsonSchema.schema {
      type = JsonSchemaType.ARRAY
      containsSchema = JsonSchema.schemaBuilder  {
        anyOfSchemas = listOf(
            schemaBuilder { constValue = 3.toJsonLiteral() },
            schemaBuilder { constValue = 4.0.toJsonLiteral() },
            schemaBuilder  { constValue = "5".toJsonLiteral() })
      }
    }

    val testValidator = ValidationMocks.createTestValidator(containsSchema)
    val invalidArray = listOf(24, "Bob", 5).toKtArray()

    val validate = testValidator.validate(invalidArray)
    assert(validate).isNotNull()
    val error = validate!!
    assert(error.keyword).isEqualTo(Keywords.CONTAINS)
  }

  @Test
  fun validate_Contains() {
    val containsSchema = JsonSchema.schema {
      type = JsonSchemaType.ARRAY
      containsSchema = JsonSchema.schemaBuilder().apply {
        anyOfSchemas = listOf(
            schemaBuilder { constValue = 3.toJsonLiteral() },
            schemaBuilder { constValue = 4.0.toJsonLiteral() },
            schemaBuilder { constValue = "5".toJsonLiteral() })
      }
    }

    containsSchema
        .validating(jsonArrayOf(24, "Bob", 5, 3))
        .isValid()
  }
}
