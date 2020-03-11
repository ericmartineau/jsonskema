package io.mverse.jsonschema.validation.nullaware

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.Keywords.REQUIRED
import io.mverse.jsonschema.keyword.StringSetKeyword
import io.mverse.jsonschema.validation.ValidationReport
import io.mverse.jsonschema.validation.keywords.KeywordValidator
import lang.json.JsrNull

/**
 * A specialized `required` validator that rejects null values.  This breaks the json-schema
 * spec, but makes some typical use cases easier.
 */
class NullAwareRequiredPropertyValidator(keyword: StringSetKeyword, schema: Schema)
  : KeywordValidator<StringSetKeyword>(REQUIRED, schema) {

  private val requiredProperties: Set<String> = keyword.value

  override fun validate(subject: JsonValueWithPath, parentReport: ValidationReport): Boolean {
    for (requiredProp in requiredProperties) {
      val toCheck = subject[requiredProp]
      if (toCheck == JsrNull) {
        parentReport += buildKeywordFailure(subject)
            .withError("required key [%s] not found", requiredProp)
      }
    }
    return parentReport.isValid
  }
}
