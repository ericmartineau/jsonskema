package io.mverse.jsonschema.validation

import io.mverse.jsonschema.Schema

interface SchemaValidatorFactory {
  fun getFormatValidator(input: String): FormatValidator?
  fun createValidator(schema: Schema): SchemaValidator
  companion object
}
