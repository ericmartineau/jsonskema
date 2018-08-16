package io.mverse.jsonschema.validation.keywords.array

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.ItemsKeyword
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.Keywords.ITEMS
import io.mverse.jsonschema.validation.SchemaValidator
import io.mverse.jsonschema.validation.ValidationReport
import io.mverse.jsonschema.validation.keywords.KeywordValidator

data class ArrayItemValidator(val parentSchema: Schema,
                              val allItemValidator: SchemaValidator)
  : KeywordValidator<ItemsKeyword>(ITEMS, parentSchema) {

  override fun validate(subject: JsonValueWithPath, report: ValidationReport): Boolean {
    var success = true
    subject.forEachIndex { _, item ->
      val valid = allItemValidator.validate(item, report)
      success = success && valid
    }
    return success
  }
}
