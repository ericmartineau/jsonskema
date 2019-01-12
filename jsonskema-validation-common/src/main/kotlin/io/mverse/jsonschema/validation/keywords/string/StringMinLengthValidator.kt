package io.mverse.jsonschema.validation.keywords.string

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.NumberKeyword
import io.mverse.jsonschema.validation.ValidationReport
import io.mverse.jsonschema.validation.keywords.KeywordValidator
import lang.string.codePointCount

class StringMinLengthValidator(keyword: NumberKeyword, schema: Schema) : KeywordValidator<NumberKeyword>(Keywords.MIN_LENGTH, schema) {
  private val minLength: Int = keyword.integer

  init {
    check(keyword.double >= 0) { "minLength cannot be negative" }
  }

  override fun validate(subject: JsonValueWithPath, parentReport: ValidationReport): Boolean {
    val string = subject.string ?: ""
    val actualLength = string.codePointCount()
    if (actualLength < minLength) {
      parentReport += buildKeywordFailure(subject)
          .withError("expected minLength: %d, actual: %d", minLength, actualLength)
    }
    return parentReport.isValid
  }
}
