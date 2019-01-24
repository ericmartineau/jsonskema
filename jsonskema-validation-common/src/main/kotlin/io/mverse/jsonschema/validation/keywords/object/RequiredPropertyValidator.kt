package io.mverse.jsonschema.validation.keywords.`object`

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.Keywords.REQUIRED
import io.mverse.jsonschema.keyword.StringSetKeyword
import io.mverse.jsonschema.validation.ValidationReport
import io.mverse.jsonschema.validation.keywords.KeywordValidator

class RequiredPropertyValidator(keyword: StringSetKeyword, schema: Schema)
  : KeywordValidator<StringSetKeyword>(REQUIRED, schema) {

  private val requiredProperties: Set<String> = keyword.value

  override fun validate(subject: JsonValueWithPath, parentReport: ValidationReport): Boolean {
    for (requiredProp in requiredProperties) {
      if (!subject.containsKey(requiredProp)) {
        parentReport += buildKeywordFailure(subject)
            .withError("required key [%s] not found", requiredProp)
      }
    }
    return parentReport.isValid
  }
}
