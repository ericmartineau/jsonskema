package io.mverse.jsonschema.validation.keywords.array

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.BooleanKeyword
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import io.mverse.jsonschema.validation.ValidationReport
import io.mverse.jsonschema.validation.keywords.KeywordValidator
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonLiteral

data class ArrayUniqueItemsValidator(val keyword: BooleanKeyword,
                                     override val schema: Schema,
                                     val factory: SchemaValidatorFactory) : KeywordValidator<BooleanKeyword>(Keywords.UNIQUE_ITEMS, schema) {

  private val requireUnique: Boolean = keyword.value

  override fun validate(subject: JsonValueWithPath, parentReport: ValidationReport): Boolean {
    if (!requireUnique) {
      return true
    }
    if (subject.arraySize == 0) {
      return true
    }

    val uniqueItems = mutableListOf<JsonElement>()
    val arrayItems = subject.jsonArray!!

    for (item in arrayItems) {
      for (contained in uniqueItems) {
        val equals = when(contained) {
          is JsonLiteral-> contained.equalsLexically(item)
          else-> contained == item
        }
        if (equals) {
          parentReport += buildKeywordFailure(subject)
              .copy(errorMessage = "array items are not unique",
                    arguments = listOf(item, contained))
          return false
        }
      }
      uniqueItems += item
    }
    return true
  }
}
