package io.mverse.jsonschema.validation

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.validation.ValidationErrorHelper.buildKeywordFailure

class FalseSchemaValidator(override val schema: Schema) : SchemaValidator {

  override fun validate(subject: JsonValueWithPath, parentReport: ValidationReport): Boolean {
    parentReport += buildKeywordFailure(subject, schema, null)
        .withError("no value allowed, found [%s]", subject.wrapped)

    return parentReport.isValid
  }
}
