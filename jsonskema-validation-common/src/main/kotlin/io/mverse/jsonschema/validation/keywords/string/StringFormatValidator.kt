package io.mverse.jsonschema.validation.keywords.string

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.Keywords.FORMAT
import io.mverse.jsonschema.keyword.StringKeyword
import io.mverse.jsonschema.validation.FormatValidator
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import io.mverse.jsonschema.validation.ValidationError
import io.mverse.jsonschema.validation.ValidationReport
import io.mverse.jsonschema.validation.keywords.KeywordValidator

class StringFormatValidator(keyword: StringKeyword, schema: Schema, factory: SchemaValidatorFactory) : KeywordValidator<StringKeyword>(FORMAT, schema) {

  private val formatValidator: FormatValidator? = factory.getFormatValidator(keyword.value)

  override fun validate(subject: JsonValueWithPath, parentReport: ValidationReport): Boolean {
    if (formatValidator == null) {
      return true
    }
    val stringSubject = subject.string!!
    val error = formatValidator.validate(stringSubject)
    if (error != null) {
      parentReport += ValidationError(violatedSchema = schema,
          pointerToViolation = subject.path,
          arguments = listOf(stringSubject),
          keyword = FORMAT,
          code = "validation.format." + formatValidator.formatName())
          .withError(error)
    }
    return parentReport.isValid
  }
}
