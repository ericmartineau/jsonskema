package io.mverse.jsonschema.validation.keywords.string

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.NumberKeyword
import io.mverse.jsonschema.validation.ValidationReport
import io.mverse.jsonschema.validation.keywords.KeywordValidator
import lang.string.codePointCount

class StringMaxLengthValidator(keyword: NumberKeyword, schema: Schema) : KeywordValidator<NumberKeyword>(Keywords.MAX_LENGTH, schema) {
  private val maxLength: Int = keyword.integer

  init {
    check(keyword.double >= 0) { "maxLength cannot be negative" }
  }

  override fun validate(subject: JsonValueWithPath, parentReport: ValidationReport): Boolean {
    val string = subject.string ?: ""
    val actualLength = string.codePointCount()
    if (actualLength > maxLength) {
      parentReport += buildKeywordFailure(subject)
          .withError("expected maxLength: %d, actual: %d", maxLength, actualLength)
    }
    return parentReport.isValid
  }
}
