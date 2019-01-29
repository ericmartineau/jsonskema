package io.mverse.jsonschema.validation.keywords.array

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.ItemsKeyword
import io.mverse.jsonschema.keyword.Keywords.ITEMS
import io.mverse.jsonschema.validation.SchemaValidator
import io.mverse.jsonschema.validation.ValidationReport
import io.mverse.jsonschema.validation.keywords.KeywordValidator

data class ArrayPerItemValidator(override val schema: Schema,
                                 val indexedValidators: List<SchemaValidator>,
                                 private val additionalItemValidator: SchemaValidator?)
  : KeywordValidator<ItemsKeyword>(ITEMS, schema) {

  override fun validate(subject: JsonValueWithPath, parentReport: ValidationReport): Boolean {
    var success = true
    val indexedValidatorCount = indexedValidators.size
    subject.forEachIndex { idx, item ->
      val valid: Boolean
      when {
        indexedValidatorCount > idx -> {
          valid = indexedValidators[idx].validate(item, parentReport)
          success = success && valid
        }
        additionalItemValidator != null -> {
          val additionalItemsValid = additionalItemValidator.validate(item, parentReport)
          valid = additionalItemsValid
        }
        else -> valid = true
      }
      success = success && valid
    }
    return success
  }
}
