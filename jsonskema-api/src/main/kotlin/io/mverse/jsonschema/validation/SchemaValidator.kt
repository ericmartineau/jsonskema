package io.mverse.jsonschema.validation


import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.JsonValueWithPath.Companion.fromJsonValue
import io.mverse.jsonschema.Schema
import kotlinx.serialization.json.JsonElement

interface SchemaValidator {

  val schema: Schema

  fun validate(subject: JsonValueWithPath, report: ValidationReport): Boolean

  fun validate(subject: JsonElement): ValidationError? {
    val pathAwareSubject = fromJsonValue(subject, subject, schema.location)
    val report = validate(pathAwareSubject)
    return ValidationError.collectErrors(schema, pathAwareSubject.path, report.errors)
  }

  fun validate(subject: JsonValueWithPath): ValidationReport {
    val report = ValidationReport()
    validate(subject, report)
    return report
  }
}
