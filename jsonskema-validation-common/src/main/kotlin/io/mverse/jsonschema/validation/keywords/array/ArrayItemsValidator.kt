package io.mverse.jsonschema.validation.keywords.array

import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.ItemsKeyword
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import io.mverse.jsonschema.validation.keywords.KeywordValidator

object ArrayItemsValidator {
  fun getArrayItemsValidator(keyword: ItemsKeyword, schema: Schema, factory: SchemaValidatorFactory): KeywordValidator<ItemsKeyword>? {
    /*
      json-schema validates arrays in two modes:
      indexed: - you provide a validator at each index that only validates a value at that index
      allItems - you provide a single validator for any item in the array
     */
    return when {

      keyword.hasIndexedSchemas -> {
        val additionItemValidator = keyword.additionalItemSchema
            ?.let { factory.createValidator(it) }

        val indexedValidators = keyword.indexedSchemas
            .map { factory.createValidator(it) }

        ArrayPerItemValidator(schema = schema,
            indexedValidators = indexedValidators,
            additionalItemValidator = additionItemValidator)
      }

      keyword.allItemSchema != null -> {
        @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
        val allItemValidator = factory.createValidator(schema = keyword.allItemSchema!!)
        ArrayItemValidator(parentSchema = schema,
            allItemValidator = allItemValidator)
      }
      else -> null
    }
  }
}
