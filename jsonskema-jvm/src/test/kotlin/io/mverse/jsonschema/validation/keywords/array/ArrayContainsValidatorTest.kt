package io.mverse.jsonschema.validation.keywords.array

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.ValidationMocks
import io.mverse.jsonschema.assertj.asserts.isValid
import io.mverse.jsonschema.assertj.asserts.validating
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.schemaBuilder
import lang.json.jsonArrayOf
import lang.json.toJsonArray
import org.junit.Test

class ArrayContainsValidatorTest {
  @Test
  fun validate_DoesntContains() {
    val containsSchema = JsonSchema.schemaBuilder()
        .type(JsonSchemaType.ARRAY)
        .containsSchema(JsonSchema.schemaBuilder()
            .anyOfSchema(JsonSchema.schemaBuilder().constValueInt(3))
            .anyOfSchema(JsonSchema.schemaBuilder().constValueDouble(4.0))
            .anyOfSchema(JsonSchema.schemaBuilder().constValueString("5")))
        .build()

    val testValidator = ValidationMocks.createTestValidator(containsSchema)
    val invalidArray = listOf(24, "Bob", 5).toJsonArray()

    val validate = testValidator.validate(invalidArray)
    assert(validate).isNotNull()
    val error = validate!!
    assert(error.keyword).isEqualTo(Keywords.CONTAINS)
  }

  @Test
  fun validate_Contains() {
    val containsSchema = JsonSchema.schemaBuilder()
        .type(JsonSchemaType.ARRAY)
        .containsSchema(JsonSchema.schemaBuilder()
            .anyOfSchema(JsonSchema.schemaBuilder().constValueDouble(3.0))
            .anyOfSchema(JsonSchema.schemaBuilder().constValueDouble(4.0))
            .anyOfSchema(JsonSchema.schemaBuilder().constValueString("5")))
        .build()

    containsSchema
        .validating(jsonArrayOf(24, "Bob", 5, 3))
        .isValid()
  }
}
