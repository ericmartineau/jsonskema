package io.mverse.jsonschema.validation

import com.google.common.base.Preconditions.checkNotNull

import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.validation.ValidationErrorHelper.TYPE_MISMATCH_ERROR_MESSAGE

object TestErrorHelper {
  fun failure(schema: Schema, desired: JsonSchemaType, found: JsonSchemaType): ValidationError {
    return ValidationError(
        violatedSchema = schema,
        messageTemplate = TYPE_MISMATCH_ERROR_MESSAGE,
        arguments = listOf(desired.name.toLowerCase(), found.name.toLowerCase()),
        keyword = Keywords.TYPE,
        pointerToViolation = schema.location.jsonPath)
  }
}
