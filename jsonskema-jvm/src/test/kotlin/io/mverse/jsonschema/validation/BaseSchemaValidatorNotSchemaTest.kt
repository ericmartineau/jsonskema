package io.mverse.jsonschema.validation

import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.validation.ValidationMocks.createTestValidator
import io.mverse.jsonschema.validation.ValidationMocks.mockBooleanSchema
import io.mverse.jsonschema.validation.ValidationTestSupport.expectSuccess

import io.mverse.jsonschema.ValidationMocks.createTestValidator
import io.mverse.jsonschema.schemaBuilder
import lang.json.toJsonLiteral
import org.junit.Test


class BaseSchemaValidatorNotSchemaTest {
  @Test
  fun failure() {
    val subject = JsonSchema.schemaBuilder().notSchema(mockBooleanSchema()).build()
    ValidationTestSupport.failureOf(subject)
        .validator(createTestValidator(subject))
        .input(true.toJsonLiteral())
        .expectedKeyword("not")
        .expect()
  }

  @Test
  fun success() {
    val schemaWithNot =JsonSchema.schemaBuilder().notSchema(mockBooleanSchema()).build()
    expectSuccess { ValidationMocks.createTestValidator(schemaWithNot).validate("foo".toJsonLiteral()) }
  }
}
