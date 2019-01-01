package io.mverse.jsonschema.validation.keywords.`object`

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.JsonValueWithPath.Companion.fromJsonValue
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.SingleSchemaKeyword
import io.mverse.jsonschema.validation.SchemaValidator
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import io.mverse.jsonschema.validation.ValidationReport
import io.mverse.jsonschema.validation.keywords.KeywordValidator
import lang.json.toJsonLiteral

class PropertyNameValidator(keyword: SingleSchemaKeyword, schema: Schema, factory: SchemaValidatorFactory) : KeywordValidator<SingleSchemaKeyword>(Keywords.PROPERTY_NAMES, schema) {

  private val propertyNameValidator: SchemaValidator = factory.createValidator(keyword.value)

  override fun validate(subject: JsonValueWithPath, parentReport: ValidationReport): Boolean {

    val report = parentReport.createChildReport()
    val subjectProperties = subject.jsonObject!!.keys
    for (subjectProperty in subjectProperties) {
      val value = subjectProperty.toJsonLiteral()
      propertyNameValidator.validate(fromJsonValue(subject.root, value, subject.location), report)
    }

    val errors = report.errors
    if (!errors.isEmpty()) {
      parentReport += buildKeywordFailure(subject)
          .copy(errorMessage = "Invalid property names",
              causes = errors)
    }
    return parentReport.isValid
  }
}
