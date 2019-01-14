package io.mverse.jsonschema.validation.keywords

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.keyword.Keywords.TYPE
import io.mverse.jsonschema.keyword.TypeKeyword
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import io.mverse.jsonschema.validation.ValidationErrorHelper.buildTypeMismatchError
import io.mverse.jsonschema.validation.ValidationReport
import lang.isIntegral

class TypeValidator(val keyword: TypeKeyword,
                    private val parent: Schema,
                    val factory: SchemaValidatorFactory) : KeywordValidator<TypeKeyword>(TYPE, parent) {

  private val requiredTypes: Set<JsonSchemaType> = keyword.types
  private val requiresInteger: Boolean = requiredTypes.contains(JsonSchemaType.INTEGER) && !this.requiredTypes.contains(JsonSchemaType.NUMBER)

  override fun validate(subject: JsonValueWithPath, parentReport: ValidationReport): Boolean {
    val valueType = subject.type
    val schemaType: JsonSchemaType
    if (requiresInteger && valueType.name == "NUMBER") {
      schemaType = if (subject.number!!.isIntegral()) JsonSchemaType.INTEGER else JsonSchemaType.NUMBER
    } else {
      schemaType = subject.jsonSchemaType
    }
    if (!requiredTypes.contains(schemaType)) {
      parentReport += buildTypeMismatchError(subject, parent, requiredTypes)
    }
    return parentReport.isValid
  }
}
