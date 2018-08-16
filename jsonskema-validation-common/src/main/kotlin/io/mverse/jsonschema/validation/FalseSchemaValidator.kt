package io.mverse.jsonschema.validation

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.utils.Schemas.falseSchema
import io.mverse.jsonschema.utils.Schemas.nullSchema
import io.mverse.jsonschema.validation.ValidationErrorHelper.buildKeywordFailure

class FalseSchemaValidator private constructor() : SchemaValidator {

  override val schema: Schema
    get() = falseSchema

  override fun validate(subject: JsonValueWithPath, parentReport: ValidationReport): Boolean {
    parentReport += buildKeywordFailure(subject, nullSchema, null)
        .withError("no value allowed, found [%s]", subject.wrapped)

    return parentReport.isValid
  }

  companion object {

    val instance = FalseSchemaValidator()
  }
}
