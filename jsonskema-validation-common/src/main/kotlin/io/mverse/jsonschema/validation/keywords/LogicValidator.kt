package io.mverse.jsonschema.validation.keywords

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.SingleSchemaKeyword
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import io.mverse.jsonschema.validation.ValidationReport

data class LogicValidator(val keyword: SingleSchemaKeyword,
                          val parent: Schema,
                          val factory: SchemaValidatorFactory) : KeywordValidator<SingleSchemaKeyword>(Keywords.IF, parent) {

  private val ifValidator = schema.asDraft7().ifSchema?.let { factory.createValidator(it) }
  private val thenValidator = schema.asDraft7().thenSchema?.let { factory.createValidator(it) }
  private val elseValidator = schema.asDraft7().elseSchema?.let { factory.createValidator(it) }

  override fun validate(subject: JsonValueWithPath, parentReport: ValidationReport): Boolean {
    if (ifValidator == null) {
      return true
    }

    val report = parentReport.createChildReport()
    return if (ifValidator.validate(subject, report)) {
      thenValidator?.validate(subject, parentReport) ?: true
    } else {
      elseValidator?.validate(subject, parentReport) ?: true
    }
  }
}
