package io.mverse.jsonschema.validation.keywords

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.SingleSchemaKeyword
import io.mverse.jsonschema.validation.SchemaValidator
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import io.mverse.jsonschema.validation.ValidationReport
import lang.Pattern

class AdditionalPropertiesValidator(keyword: SingleSchemaKeyword, schema: Schema, factory: SchemaValidatorFactory) : KeywordValidator<SingleSchemaKeyword>(Keywords.ADDITIONAL_PROPERTIES, schema) {

  private val additionalPropertiesValidator: SchemaValidator

  private val propertySchemaKeys: Set<String>

  private val patternProperties: Set<Pattern>

  init {
    val draft6Schema = schema.asDraft6()
    this.additionalPropertiesValidator = factory.createValidator(keyword.value)
    this.patternProperties = draft6Schema.patternProperties.keys
        .map { Pattern(it) }
        .toSet()

    this.propertySchemaKeys = draft6Schema.properties.keys
  }

  override fun validate(subject: JsonValueWithPath, parentReport: ValidationReport): Boolean {
    val report = parentReport.createChildReport()

    prop@ for (propName in subject.propertyNames()) {
      for (pattern in patternProperties) {
        if (pattern.find(propName)) {
          continue@prop
        }
      }
      if (!propertySchemaKeys.contains(propName)) {
        val propertyValue = subject.path(propName)
        additionalPropertiesValidator.validate(propertyValue, report)
      }
    }
    if (!report.isValid) {
      parentReport.addReport(schema, subject, Keywords.ADDITIONAL_PROPERTIES, "Additional properties were invalid", report)
    }
    return parentReport.isValid
  }
}
