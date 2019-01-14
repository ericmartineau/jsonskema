package io.mverse.jsonschema.validation.keywords

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.JsonArrayKeyword
import io.mverse.jsonschema.keyword.Keywords.ENUM
import io.mverse.jsonschema.utils.equalsLexically
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import io.mverse.jsonschema.validation.ValidationReport
import lang.json.JsrValue
import lang.json.values

data class EnumValidator(val keyword: JsonArrayKeyword,
                         val parent: Schema,
                         val factory: SchemaValidatorFactory)
  : KeywordValidator<JsonArrayKeyword>(ENUM, parent) {

  private val enumValues: List<JsrValue> = keyword.value.values

  override fun validate(subject: JsonValueWithPath, parentReport: ValidationReport): Boolean {
    for (enumValue in enumValues) {
      val eq = enumValue.equalsLexically(subject.wrapped)
      if (eq) return true
    }
    parentReport += buildKeywordFailure(subject)
        .withError("%s does not match the enum values", subject)

    return parentReport.isValid
  }
}
