package io.mverse.jsonschema.validation.keywords.string

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.StringKeyword
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import io.mverse.jsonschema.validation.ValidationReport
import io.mverse.jsonschema.validation.keywords.KeywordValidator
import lang.Pattern

class StringPatternValidator(keyword: StringKeyword, schema: Schema) : KeywordValidator<StringKeyword>(Keywords.PATTERN, schema) {

  private val pattern: Pattern = Pattern(keyword.value)

  override fun validate(subject: JsonValueWithPath, parentReport: ValidationReport): Boolean {
    val stringSubject = subject.string ?: ""
    if (!patternMatches(pattern, stringSubject)) {
      parentReport += buildKeywordFailure(subject)
          .withError("string [%s] does not match pattern %s", stringSubject, pattern.regex)
    }
    return parentReport.isValid
  }

  private fun patternMatches(pattern: Pattern, string: String): Boolean {
    return pattern.find(string)
  }
}
