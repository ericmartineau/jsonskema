package io.mverse.jsonschema.validation

import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.JsonSchema.schema
import io.mverse.jsonschema.schema
import io.mverse.jsonschema.schemaBuilder
import io.mverse.jsonschema.validation.ValidationMocks.createTestValidator
import io.mverse.jsonschema.validation.ValidationMocks.mockBooleanSchema
import io.mverse.jsonschema.validation.ValidationTestSupport.expectSuccess
import lang.json.JsrTrue
import lang.json.toJsrValue
import org.junit.Test

class BaseSchemaValidatorNotSchemaTest {
  @Test
  fun failure() {
    val subject = schema { notSchema = mockBooleanSchema }
    ValidationTestSupport.failureOf(subject)
        .validator(createTestValidator(subject))
        .input(JsrTrue)
        .expectedKeyword("not")
        .expect()
  }

  @Test
  fun success() {
    val schemaWithNot = schemaBuilder { notSchema = mockBooleanSchema }
    expectSuccess { ValidationMocks.createTestValidator(schemaWithNot).validate("foo".toJsrValue()) }
  }
}
