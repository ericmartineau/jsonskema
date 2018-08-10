package io.mverse.jsonschema.validation.keywords

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.Keywords.Companion.NOT
import io.mverse.jsonschema.keyword.SingleSchemaKeyword
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import io.mverse.jsonschema.validation.ValidationReport

data class NotKeywordValidator(val keyword: SingleSchemaKeyword,
                               private val notSchema: Schema,
                               val factory: SchemaValidatorFactory) : KeywordValidator<SingleSchemaKeyword>(NOT, notSchema) {
  private val notValidator = factory.createValidator(keyword.schema)

  override fun validate(subject: JsonValueWithPath, report: ValidationReport): Boolean {
    val trap = report.createChildReport()
    if (notValidator.validate(subject, trap)) {
      report += buildKeywordFailure(subject)
          .withError("subject must not be valid against schema", notSchema.pointerFragmentURI ?: "")
    }
    return report.isValid
  }
}
