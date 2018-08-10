package io.mverse.jsonschema.validation.keywords.`object`

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.Keywords.Companion.REQUIRED
import io.mverse.jsonschema.keyword.StringSetKeyword
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import io.mverse.jsonschema.validation.ValidationReport
import io.mverse.jsonschema.validation.keywords.KeywordValidator

class RequiredPropertyValidator(keyword: StringSetKeyword, schema: Schema, factory: SchemaValidatorFactory)
  : KeywordValidator<StringSetKeyword>(REQUIRED, schema) {

  private val requiredProperties: Set<String> = keyword.stringSet

  override fun validate(subject: JsonValueWithPath, report: ValidationReport): Boolean {
    for (requiredProp in requiredProperties) {
      if (!subject.containsKey(requiredProp)) {
        report += buildKeywordFailure(subject)
            .withError("required key [%s] not found", requiredProp)
      }
    }
    return report.isValid
  }
}
