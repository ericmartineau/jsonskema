package io.mverse.jsonschema.validation

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.Keywords.TYPE
import lang.Joiner

object ValidationErrorHelper {

  const val TYPE_MISMATCH_ERROR_MESSAGE = "expected type: %s, found: %s"
  const val VALIDATION_KEYWORD_PREFIX = "validation.keyword."

  fun buildKeywordFailure(subject: JsonValueWithPath, schema: Schema, keyword: KeywordInfo<*>?): ValidationError {
    return ValidationError(violatedSchema = schema,
        pointerToViolation = subject.path,
        keyword = keyword,
        code = VALIDATION_KEYWORD_PREFIX + keyword)
  }

  fun buildTypeMismatchError(subject: JsonValueWithPath, schema: Schema, expectedTypes: Collection<JsonSchemaType>): ValidationError {
    if (expectedTypes.size == 1) {
      return buildTypeMismatchError(subject, schema, expectedTypes.iterator().next())
    }

    val commaSeparatedTypes = Joiner(",").join(expectedTypes)

    return ValidationError(violatedSchema = schema,
        pointerToViolation = subject.path,
        keyword = TYPE,
        code = "validation.typeMismatch",
        messageTemplate = "expected one of the following types: %s, found: %s",
        arguments = listOf(commaSeparatedTypes, subject.jsonSchemaType))
  }

  fun buildTypeMismatchError(subject: JsonValueWithPath, schema: Schema, expectedType: JsonSchemaType): ValidationError {
    return ValidationError(violatedSchema = schema,
        pointerToViolation = subject.path,
        keyword = Keywords.TYPE,
        messageTemplate = TYPE_MISMATCH_ERROR_MESSAGE,
        arguments = listOf(expectedType, subject.jsonSchemaType),
        code = "validation.typeMismatch")
  }
}
