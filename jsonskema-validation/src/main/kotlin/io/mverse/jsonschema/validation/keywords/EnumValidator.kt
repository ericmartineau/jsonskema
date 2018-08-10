package io.mverse.jsonschema.validation.keywords

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.JsonArrayKeyword
import io.mverse.jsonschema.keyword.Keywords.Companion.ENUM
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import io.mverse.jsonschema.validation.ValidationReport
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

data class EnumValidator(val keyword: JsonArrayKeyword,
                         val parent: Schema,
                         val factory: SchemaValidatorFactory)
  : KeywordValidator<JsonArrayKeyword>(ENUM, parent) {

  private val enumValues: List<JsonElement> = keyword.jsonArray

  override fun validate(subject: JsonValueWithPath, report: ValidationReport): Boolean {
    for (enumValue in enumValues) {
      return when (enumValue) {
        is JsonPrimitive -> enumValue.equalsLexically(subject.wrapped)
        else -> enumValue == subject.wrapped
      }
    }
    report += buildKeywordFailure(subject)
        .withError("%s does not match the enum values", subject)

    return report.isValid
  }
}
