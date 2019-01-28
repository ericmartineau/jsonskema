package io.mverse.jsonschema.validation

import io.mverse.jsonschema.DraftSchema
import io.mverse.jsonschema.Schema

interface SchemaValidatorFactory {

  fun getFormatValidator(input: String): FormatValidator?

  fun createValidator(schema: Schema): SchemaValidator
  fun createValidator(draftSchema: DraftSchema): SchemaValidator = createValidator(draftSchema.schema)
}
