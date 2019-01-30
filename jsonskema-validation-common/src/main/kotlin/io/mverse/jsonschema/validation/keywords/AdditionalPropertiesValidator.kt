package io.mverse.jsonschema.validation.keywords

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.Keywords.ADDITIONAL_PROPERTIES
import io.mverse.jsonschema.keyword.SingleSchemaKeyword
import io.mverse.jsonschema.validation.SchemaValidator
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import io.mverse.jsonschema.validation.ValidationReport

class AdditionalPropertiesValidator(keyword: SingleSchemaKeyword, schema: Schema, factory: SchemaValidatorFactory) : KeywordValidator<SingleSchemaKeyword>(Keywords.ADDITIONAL_PROPERTIES, schema) {

  private val additionalPropertiesValidator: SchemaValidator
  private val propertySchemaKeys: Set<String>
  private val patternProperties: Set<Regex>

  init {
    val draft6Schema = schema.draft6()
    this.additionalPropertiesValidator = factory.createValidator(keyword.value)
    this.patternProperties = draft6Schema.patternProperties.keys
        .map { Regex(it) }
        .toSet()

    this.propertySchemaKeys = draft6Schema.properties.keys
  }

  override fun validate(subject: JsonValueWithPath, parentReport: ValidationReport): Boolean {
    val invalidProps = mutableMapOf<JsonValueWithPath, ValidationReport>()

    prop@ for (propName in subject.propertyNames()) {
      for (pattern in patternProperties) {
        if (pattern.containsMatchIn(propName)) {
          continue@prop
        }
      }
      if (!propertySchemaKeys.contains(propName)) {
        val propertyValue = subject.path(propName)
        val addtlProps = parentReport.createChildReport()
        additionalPropertiesValidator.validate(propertyValue, addtlProps)
        if (!addtlProps.isValid) {
          invalidProps += propertyValue to addtlProps
        }
      }
    }
    invalidProps.forEach { (subject) ->
      parentReport += buildKeywordFailure(subject)
          .copy(keyword = ADDITIONAL_PROPERTIES,
              code = "validation.keyword.additionalProperties",
              messageTemplate = "Invalid additional property '%s'",
              arguments = listOf(subject.path.jsonPtr),
              pointerToViolation = subject.path
          )
    }
    return parentReport.isValid
  }
}
