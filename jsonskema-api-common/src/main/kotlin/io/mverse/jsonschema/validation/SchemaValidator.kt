package io.mverse.jsonschema.validation

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.JsonValueWithPath.Companion.fromJsonValue
import io.mverse.jsonschema.Schema
import kotlinx.serialization.json.JsonElement
import lang.Name
import lang.json.JsrValue
import lang.json.toJsrValue

interface SchemaValidator {

  val schema: Schema

  @Name("validateWithParentReport")
  fun validate(subject: JsonValueWithPath, parentReport: ValidationReport): Boolean

  @Name("validateJsrValue")
  fun validate(subject: JsrValue): ValidationError? {
    val pathAwareSubject = fromJsonValue(subject, subject, schema.location)
    val report = validate(pathAwareSubject)
    return ValidationError.collectErrors(schema, pathAwareSubject.path, report.errors)
  }

  @Name("validateKtValue")
  fun validateKt(subject: JsonElement): ValidationError? = validate(subject.toJsrValue())

  @Name("validate")
  fun validate(subject: JsonValueWithPath): ValidationReport {
    val report = ValidationReport()
    validate(subject, report)
    return report
  }

  companion object
}
