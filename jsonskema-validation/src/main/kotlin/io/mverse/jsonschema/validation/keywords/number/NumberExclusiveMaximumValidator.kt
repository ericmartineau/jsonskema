package io.mverse.jsonschema.validation.keywords.number

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.Keywords.Companion.EXCLUSIVE_MAXIMUM
import io.mverse.jsonschema.keyword.LimitKeyword
import io.mverse.jsonschema.validation.ValidationReport
import io.mverse.jsonschema.validation.keywords.KeywordValidator

class NumberExclusiveMaximumValidator(schema: Schema, private val exclusiveMaximum: Double) : KeywordValidator<LimitKeyword>(EXCLUSIVE_MAXIMUM, schema) {

  override fun validate(subject: JsonValueWithPath, report: ValidationReport): Boolean {
    val subjectDouble = subject.number!!.toDouble()

    if (subjectDouble >= exclusiveMaximum) {
      report += buildKeywordFailure(subject)
          .withError("Value is not lower than %s", exclusiveMaximum)
    }

    return report.isValid
  }
}
