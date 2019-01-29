package io.mverse.jsonschema.validation

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.validation.ValidationErrorHelper.buildTypeMismatchError

class NullSchemaValidator(override val schema: Schema) : SchemaValidator {

  override fun validate(subject: JsonValueWithPath, parentReport: ValidationReport): Boolean {
    if (subject.isNotNull) {
      parentReport += buildTypeMismatchError(subject, schema, JsonSchemaType.NULL)
    }
    return parentReport.isValid
  }
}
