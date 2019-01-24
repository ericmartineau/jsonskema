package io.mverse.jsonschema.validation.keywords.array

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.Keywords.MAX_ITEMS
import io.mverse.jsonschema.keyword.NumberKeyword
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import io.mverse.jsonschema.validation.ValidationReport
import io.mverse.jsonschema.validation.keywords.KeywordValidator
import lang.json.values

data class ArrayMaxItemsValidator(val number: NumberKeyword,
                                  override val schema: Schema,
                                  val factory: SchemaValidatorFactory)
  : KeywordValidator<NumberKeyword>(MAX_ITEMS, schema) {

  private val maxItems: Int = number.integer

  init {
    check(maxItems >= 0) { "maxItems can't be negative" }
  }

  override fun validate(subject: JsonValueWithPath, parentReport: ValidationReport): Boolean {
    val actualLength = subject.jsonArray!!.values.size

    if (actualLength > maxItems) {
      parentReport += buildKeywordFailure(subject)
          .withError("expected maximum item count: %s, found: %s", maxItems, actualLength)
    }
    return parentReport.isValid
  }
}
