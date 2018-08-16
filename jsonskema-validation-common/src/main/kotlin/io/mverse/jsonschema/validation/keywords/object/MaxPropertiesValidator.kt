package io.mverse.jsonschema.validation.keywords.`object`

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.NumberKeyword
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import io.mverse.jsonschema.validation.ValidationReport
import io.mverse.jsonschema.validation.keywords.KeywordValidator

class MaxPropertiesValidator(keyword: NumberKeyword, schema: Schema) : KeywordValidator<NumberKeyword>(Keywords.MAX_PROPERTIES, schema) {
  private val maxProperties: Int = keyword.integer

  init {
    check(maxProperties >= 0) { "maxProperties can't be negative" }
  }

  override fun validate(subject: JsonValueWithPath, parentReport: ValidationReport): Boolean {
    val actualSize = subject.numberOfProperties()
    if (actualSize > maxProperties) {
      parentReport += buildKeywordFailure(subject)
          .withError("maximum size: [%d], found: [%d]", maxProperties, actualSize)
    }
    return parentReport.isValid
  }
}
