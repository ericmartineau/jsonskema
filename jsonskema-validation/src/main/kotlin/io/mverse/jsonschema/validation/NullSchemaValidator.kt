package io.mverse.jsonschema.validation

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.utils.Schemas.nullSchema
import io.mverse.jsonschema.validation.ValidationErrorHelper.buildTypeMismatchError

class NullSchemaValidator private constructor() : SchemaValidator {

  override val schema: Schema
    get() = nullSchema

  override fun validate(subject: JsonValueWithPath, report: ValidationReport): Boolean {
    if (subject.isNotNull) {
      report += buildTypeMismatchError(subject, nullSchema, JsonSchemaType.NULL)
    }
    return report.isValid
  }

  companion object {
    val instance = NullSchemaValidator()
  }
}
