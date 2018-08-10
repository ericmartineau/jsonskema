package io.mverse.jsonschema.validation.keywords

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.JsonSchemaKeyword
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.SchemaKeyword
import io.mverse.jsonschema.validation.SchemaValidator
import io.mverse.jsonschema.validation.ValidationError
import io.mverse.jsonschema.validation.ValidationErrorHelper

abstract class KeywordValidator<K : JsonSchemaKeyword<*>>(private val keyword: KeywordInfo<K>,
                                                          override val schema: Schema) : SchemaValidator {

  protected fun buildKeywordFailure(location: JsonValueWithPath): ValidationError {
    return ValidationErrorHelper.buildKeywordFailure(location, schema, keyword)
  }
}
