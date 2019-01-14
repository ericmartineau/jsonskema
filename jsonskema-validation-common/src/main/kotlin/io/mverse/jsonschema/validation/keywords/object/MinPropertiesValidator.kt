package io.mverse.jsonschema.validation.keywords.`object`

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.NumberKeyword
import io.mverse.jsonschema.validation.ValidationReport
import io.mverse.jsonschema.validation.keywords.KeywordValidator

class MinPropertiesValidator(number: NumberKeyword, schema: Schema) : KeywordValidator<NumberKeyword>(Keywords.MIN_PROPERTIES, schema) {
  private val minProperties: Int = number.integer

  init {
    check(minProperties >= 0) { "minProperties can't be negative" }
  }

  override fun validate(subject: JsonValueWithPath, parentReport: ValidationReport): Boolean {
    val actualSize = subject.numberOfProperties()
    if (actualSize < minProperties) {
      parentReport += buildKeywordFailure(subject)
          .withError("minimum size: [%d], found: [%d]", minProperties, actualSize)
    }
    return parentReport.isValid
  }
}
