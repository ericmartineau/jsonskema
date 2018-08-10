package io.mverse.jsonschema.validation.keywords.number

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.LimitKeyword
import io.mverse.jsonschema.validation.ValidationReport
import io.mverse.jsonschema.validation.keywords.KeywordValidator

class NumberMinimumValidator(schema: Schema, private val minimum: Double) : KeywordValidator<LimitKeyword>(Keywords.MINIMUM, schema) {

  override fun validate(subject: JsonValueWithPath, report: ValidationReport): Boolean {
    val subjectNumber = subject.number!!.toDouble()

    if (subjectNumber < minimum) {
      report += buildKeywordFailure(subject)
          .withError("Value is not higher or equal to %s", minimum)
    }
    return report.isValid
  }
}
