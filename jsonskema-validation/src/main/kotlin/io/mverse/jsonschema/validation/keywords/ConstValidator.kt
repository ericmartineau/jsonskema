package io.mverse.jsonschema.validation.keywords

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.JsonValueKeyword
import io.mverse.jsonschema.keyword.Keywords.Companion.CONST
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import io.mverse.jsonschema.validation.ValidationReport
import kotlinx.serialization.json.JsonElement

data class ConstValidator(val keyword: JsonValueKeyword,
                          val parentSchema: Schema,
                          val factory: SchemaValidatorFactory)
  : KeywordValidator<JsonValueKeyword>(CONST, parentSchema) {

  private val constValue: JsonElement? = keyword.keywordValue!!

  override fun validate(subject: JsonValueWithPath, report: ValidationReport): Boolean {
    if (constValue != subject.wrapped) {
      report += buildKeywordFailure(subject)
          .withError("%s does not match the const value", subject)
    }
    return report.isValid
  }
}
