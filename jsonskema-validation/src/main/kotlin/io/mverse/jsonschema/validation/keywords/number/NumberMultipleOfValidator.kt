package io.mverse.jsonschema.validation.keywords.number

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.NumberKeyword
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import io.mverse.jsonschema.validation.ValidationReport
import io.mverse.jsonschema.validation.keywords.KeywordValidator

class NumberMultipleOfValidator(numberKeyword: NumberKeyword, schema: Schema, factory: SchemaValidatorFactory) : KeywordValidator<NumberKeyword>(Keywords.MULTIPLE_OF, schema) {

  private val multipleOf: Double = numberKeyword.double


  override fun validate(subject: JsonValueWithPath, report: ValidationReport): Boolean {
    val subjectDecimal = subject.number!!.toDouble()

    val remainder = subjectDecimal.rem(multipleOf)
    if (remainder != 0.0) {
      report += buildKeywordFailure(subject)
          .withError("Value is not a multiple of %s", multipleOf)
    }
    return report.isValid
  }
}
