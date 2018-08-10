package io.mverse.jsonschema.validation

import com.google.common.base.Preconditions.checkNotNull

import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.keyword.Keywords

object TestErrorHelper {
  fun failure(schema: Schema, desired: JsonSchemaType, found: JsonSchemaType): ValidationError {
    checkNotNull(schema, "schema" + " must not be null")
    return ValidationError(
        violatedSchema = schema,
        messageTemplate = ValidationErrorHelper.TYPE_MISMATCH_ERROR_MESSAGE,
        arguments = listOf(desired, found),
        keyword = Keywords.TYPE,
        pointerToViolation = schema.location.jsonPath)
  }
}
