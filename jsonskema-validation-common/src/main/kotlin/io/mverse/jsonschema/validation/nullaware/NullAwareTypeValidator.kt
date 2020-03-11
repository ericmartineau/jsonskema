package io.mverse.jsonschema.validation.nullaware

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.keyword.Keywords.TYPE
import io.mverse.jsonschema.keyword.TypeKeyword
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import io.mverse.jsonschema.validation.ValidationErrorHelper.buildTypeMismatchError
import io.mverse.jsonschema.validation.ValidationReport
import io.mverse.jsonschema.validation.keywords.KeywordValidator
import lang.isIntegral
import lang.json.JsrType

/**
 * This is a variant of the [TypeValidator] that always accepts null values  It's assumed
 * that the parent schema will use a modified `required` validator to restrict null values.
 *
 * This solves a common problem with schema validation and null handling.
 */
class NullAwareTypeValidator(val keyword: TypeKeyword,
                             private val parent: Schema,
                             val factory: SchemaValidatorFactory) : KeywordValidator<TypeKeyword>(TYPE, parent) {

  private val requiredTypes: Set<JsonSchemaType> = keyword.types
  private val requiresInteger: Boolean = requiredTypes.contains(JsonSchemaType.INTEGER) && !this.requiredTypes.contains(JsonSchemaType.NUMBER)

  override fun validate(subject: JsonValueWithPath, parentReport: ValidationReport): Boolean {
    val valueType = subject.type
    if(valueType == JsrType.NULL) {
      return parentReport.isValid
    }
    val schemaType: JsonSchemaType
    schemaType = when {
      requiresInteger && valueType.name == "NUMBER" -> {
        if (subject.number!!.isIntegral()) JsonSchemaType.INTEGER else JsonSchemaType.NUMBER
      }
      else -> subject.jsonSchemaType
    }
    if (!requiredTypes.contains(schemaType)) {
      parentReport += buildTypeMismatchError(subject, parent, requiredTypes)
    }
    return parentReport.isValid
  }
}
