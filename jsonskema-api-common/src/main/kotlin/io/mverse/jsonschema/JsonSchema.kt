package io.mverse.jsonschema

import io.mverse.jsonschema.validation.SchemaValidator
import io.mverse.jsonschema.validation.SchemaValidatorFactory

expect object JsonSchema {
  fun createValidatorFactory(): SchemaValidatorFactory
  val validatorFactory: SchemaValidatorFactory
}

fun JsonSchema.getValidator(schema:Schema): SchemaValidator = JsonSchema.validatorFactory.createValidator(schema)
