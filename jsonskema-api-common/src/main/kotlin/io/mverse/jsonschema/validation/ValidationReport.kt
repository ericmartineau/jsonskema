package io.mverse.jsonschema.validation

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.validation.ValidationError.Companion.collectErrors
import kotlinx.serialization.Serializable
import lang.Name

@Serializable
class ValidationReport {

  @Name("errors")
  val errors = mutableListOf<ValidationError>()
  private var foundError: Boolean = false

  @Name("isValid")
  val isValid: Boolean
    get() = !foundError

  operator fun plus(validationError: ValidationError): ValidationReport {
    return apply {
      errors += validationError
      foundError = true
    }
  }

  operator fun plusAssign(validationError: ValidationError) {
    errors += validationError
    foundError = true
  }

  fun addReport(schema: Schema, subject: JsonValueWithPath, report: ValidationReport): Boolean {
    val error = collectErrors(schema, subject.path, report.errors)
    if (error != null) {
      this += error
    }
    return error == null
  }

  fun createChildReport(): ValidationReport {
    return ValidationReport()
  }

  override fun toString(): String {
    val string = StringBuilder()
    writeTo(string)
    return string.toString()
  }

  fun writeTo(printer: StringBuilder) {

    if (errors.isNotEmpty()) {
      printer.println("###############################################")
      printer.println("####              ERRORS                   ####")
      printer.println("###############################################")
    }
    errors.forEach { e -> this.toStringErrors(e, printer) }
  }

  private fun toStringErrors(error: ValidationError, printer: StringBuilder) {
    if (error.causes.isNotEmpty()) {
      error.causes.forEach { e -> toStringErrors(e, printer) }
    }
    printer.println(error.pathToViolation)
    val keywordValue = error.keyword?.key ?: "Unknown"
    printer.println("\tKeyword: $keywordValue")
    printer.println("\tMessage: " + error.message)
    printer.println("\tSchema : " + error.schemaLocation!!)
    printer.println("")
  }
}

fun StringBuilder.println(any: Any?) = append(any)