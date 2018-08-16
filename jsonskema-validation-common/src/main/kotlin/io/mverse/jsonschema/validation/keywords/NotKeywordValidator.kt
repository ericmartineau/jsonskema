package io.mverse.jsonschema.validation.keywords

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.Keywords.NOT
import io.mverse.jsonschema.keyword.SingleSchemaKeyword
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import io.mverse.jsonschema.validation.ValidationReport

data class NotKeywordValidator(val keyword: SingleSchemaKeyword,
                               private val notSchema: Schema,
                               val factory: SchemaValidatorFactory) : KeywordValidator<SingleSchemaKeyword>(NOT, notSchema) {
  private val notValidator = factory.createValidator(keyword.value)

  override fun validate(subject: JsonValueWithPath, parentReport: ValidationReport): Boolean {
    val trap = parentReport.createChildReport()
    if (notValidator.validate(subject, trap)) {
      parentReport += buildKeywordFailure(subject)
          .withError("subject must not be valid against schema", notSchema.pointerFragmentURI ?: "")
    }
    return parentReport.isValid
  }
}
