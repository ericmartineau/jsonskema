package io.mverse.jsonschema.validation.keywords.number

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.LimitKeyword
import io.mverse.jsonschema.validation.ValidationReport
import io.mverse.jsonschema.validation.keywords.KeywordValidator

class NumberMaximumValidator(schema: Schema, private val maximum: Double) : KeywordValidator<LimitKeyword>(Keywords.MAXIMUM, schema) {

  override fun validate(subject: JsonValueWithPath, parentReport: ValidationReport): Boolean {
    val subjectNumber = subject.number!!.toDouble()

    // max = 10
    // sub = 10
    // this is okay
    if (subjectNumber > maximum) {
      parentReport += buildKeywordFailure(subject)
          .withError("Value not lower or equal to %s", maximum)
    }
    return parentReport.isValid
  }
}
