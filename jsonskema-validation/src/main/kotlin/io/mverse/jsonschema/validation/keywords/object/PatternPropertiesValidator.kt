package io.mverse.jsonschema.validation.keywords.`object`

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.SchemaMapKeyword
import io.mverse.jsonschema.validation.SchemaValidator
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import io.mverse.jsonschema.validation.ValidationReport
import io.mverse.jsonschema.validation.keywords.KeywordValidator
import lang.Pattern

class PatternPropertiesValidator(keyword: SchemaMapKeyword, schema: Schema, factory: SchemaValidatorFactory) : KeywordValidator<SchemaMapKeyword>(Keywords.PATTERN_PROPERTIES, schema) {

  private val patternValidators = keyword.schemas
      .map { (regex, schema) ->
        val pattern = Pattern(regex)
        val validator = factory.createValidator(schema)
        PatternPropertyValidator(pattern, validator)
      }

  override fun validate(subject: JsonValueWithPath, parentReport: ValidationReport): Boolean {
    val subjectProperties = subject.propertyNames()
    if (subjectProperties.isEmpty()) {
      return true
    }
    var success = true
    val report = parentReport.createChildReport()
    for (patternValidator in patternValidators) {
      val pattern = patternValidator.pattern
      val validator = patternValidator.validator
      for (propertyName in subjectProperties) {
        if (pattern.find(propertyName)) {
          val propertyValue = subject.path(propertyName)
          success = success && validator.validate(propertyValue, report)
        }
      }
    }
    return parentReport.addReport(schema, subject, report)
  }

  class PatternPropertyValidator(val pattern: Pattern, val validator: SchemaValidator)
}
