package io.mverse.jsonschema.validation.keywords.number

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.LimitKeyword
import io.mverse.jsonschema.validation.ValidationReport
import io.mverse.jsonschema.validation.keywords.KeywordValidator

class NumberExclusiveMinimumValidator(schema: Schema, private val exclusiveMinimum: Double) : KeywordValidator<LimitKeyword>(Keywords.EXCLUSIVE_MINIMUM, schema) {

  override fun validate(subject: JsonValueWithPath, parentReport: ValidationReport): Boolean {
    val subjectDouble = subject.number!!.toDouble()

    if (subjectDouble <= exclusiveMinimum) {
      parentReport += buildKeywordFailure(subject)
          .withError("Value is not higher than %s", exclusiveMinimum)
    }
    return parentReport.isValid
  }
}
