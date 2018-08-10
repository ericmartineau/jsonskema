package io.mverse.jsonschema.validation.keywords.array

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.Keywords.Companion.MIN_ITEMS
import io.mverse.jsonschema.keyword.NumberKeyword
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import io.mverse.jsonschema.validation.ValidationReport
import io.mverse.jsonschema.validation.keywords.KeywordValidator

data class ArrayMinItemsValidator(val number: NumberKeyword,
                                  override val schema: Schema,
                                  val factory: SchemaValidatorFactory) : KeywordValidator<NumberKeyword>(MIN_ITEMS, schema) {
  private val minItems: Int = number.integer

  init {
    check(minItems >= 0) { "minItems can't be negative" }
  }

  override fun validate(subject: JsonValueWithPath, report: ValidationReport): Boolean {
    val actualLength = subject.arraySize

    if (actualLength < minItems) {
      report += buildKeywordFailure(subject)
          .withError("expected minimum item count: %s, found: %s", minItems, actualLength)
    }
    return report.isValid
  }
}
