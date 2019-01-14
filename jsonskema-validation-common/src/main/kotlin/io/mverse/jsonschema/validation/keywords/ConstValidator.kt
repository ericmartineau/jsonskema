package io.mverse.jsonschema.validation.keywords

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.JsonValueKeyword
import io.mverse.jsonschema.keyword.Keywords.CONST
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import io.mverse.jsonschema.validation.ValidationReport
import lang.json.JsrValue

data class ConstValidator(val keyword: JsonValueKeyword,
                          val parentSchema: Schema,
                          val factory: SchemaValidatorFactory)
  : KeywordValidator<JsonValueKeyword>(CONST, parentSchema) {

  private val constValue: JsrValue? = keyword.value

  override fun validate(subject: JsonValueWithPath, parentReport: ValidationReport): Boolean {
    if (constValue != subject.wrapped) {
      parentReport += buildKeywordFailure(subject)
          .withError("%s does not match the const value", subject)
    }
    return parentReport.isValid
  }
}
