package io.mverse.jsonschema.validation.keywords.`object`

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.Keywords.Companion.PROPERTIES
import io.mverse.jsonschema.keyword.SchemaMapKeyword
import io.mverse.jsonschema.validation.SchemaValidator
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import io.mverse.jsonschema.validation.ValidationReport
import io.mverse.jsonschema.validation.keywords.KeywordValidator

data class PropertySchemaValidator(val keyword: SchemaMapKeyword,
                                   override val schema: Schema,
                                   val factory: SchemaValidatorFactory)
  : KeywordValidator<SchemaMapKeyword>(PROPERTIES, schema) {

  private val propertyValidators: Map<String, SchemaValidator> = keyword.value
      .map { (key, schema) -> key to factory.createValidator(schema) }
      .toMap()
  private val validatedProperties: Set<String> = HashSet(propertyValidators.keys)
  private val propertyLength: Int = validatedProperties.size

  override fun validate(subject: JsonValueWithPath, report: ValidationReport): Boolean {
    val subjectProperties = subject.jsonObject.keys

    val a: Set<String>
    val b: Set<String>
    val bSmaller = subjectProperties.size < this.propertyLength
    a = if (bSmaller) subjectProperties else validatedProperties
    b = if (bSmaller) validatedProperties else subjectProperties

    for (property in a) {
      if (!b.contains(property)) {
        continue
      }
      val propValidator = propertyValidators[property]
      val pathAwareSubject = subject.path(property)
      propValidator?.validate(pathAwareSubject, report)
    }

    return report.isValid
  }
}
