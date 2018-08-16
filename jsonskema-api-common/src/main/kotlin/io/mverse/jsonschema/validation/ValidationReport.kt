package io.mverse.jsonschema.validation

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.validation.ValidationError.Companion.collectErrors
import kotlinx.io.PrintWriter
import kotlinx.io.StringWriter
import kotlinx.io.Writer

class ValidationReport {
  val errors = mutableListOf<ValidationError>()
  private var foundError: Boolean = false

  val flattenedErrors: List<ValidationError> get() = errors.map { it.allMessages }.flatten()
  val isValid: Boolean get() = !foundError

  operator fun plus(validationError: ValidationError): ValidationReport {
    return apply {
      errors += validationError
      foundError=true
    }
  }

  operator fun plusAssign(validationError: ValidationError) {
    errors += validationError
    foundError=true
  }

  fun addReport(schema: Schema,
                subject: JsonValueWithPath,
                keyword: KeywordInfo<*>,
                message: String,
                report: ValidationReport): Boolean {
    val errors = report.errors
    if (report.errors.isNotEmpty()) {
      this += ValidationError(
          violatedSchema = schema,
          causes = errors,
          keyword = keyword,
          errorMessage = message,
          code = "validation.keyword.${keyword.key}",
          pointerToViolation = subject.path)
      return false
    }
    return true
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
    val string = StringWriter()
    writeTo(string)
    return string.toString()
  }

  fun writeTo(writer: Writer) {
    val printer = PrintWriter(writer)
    if (errors.isNotEmpty()) {
      printer.println("###############################################")
      printer.println("####              ERRORS                   ####")
      printer.println("###############################################")
    }
    errors.forEach { e -> this.toStringErrors(e, printer) }
  }

  private fun toStringErrors(error: ValidationError, printer: PrintWriter) {
    if (error.causes!!.isNotEmpty()) {
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
