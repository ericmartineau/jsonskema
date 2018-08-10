package io.mverse.jsonschema.validation.keywords

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.Keywords.Companion.ANY_OF
import io.mverse.jsonschema.keyword.SchemaListKeyword
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import io.mverse.jsonschema.validation.ValidationReport

data class AnyOfValidator(val keyword: SchemaListKeyword, override val schema: Schema, val factory: SchemaValidatorFactory) : KeywordValidator<SchemaListKeyword>(ANY_OF, schema) {

  private val anyOfValidators = keyword.subschemas.map { factory.createValidator(it) }

  override fun validate(subject: JsonValueWithPath, parentReport: ValidationReport): Boolean {
    val anyOfReport = parentReport.createChildReport()
    for (anyOfValidator in anyOfValidators) {
      val trap = anyOfReport.createChildReport()
      if (anyOfValidator.validate(subject, trap)) {
        return true
      }
      anyOfReport.addReport(schema, subject, trap)
    }

    parentReport += buildKeywordFailure(subject)
        .copy(
            errorMessage = "no subschema matched out of the total %d subschemas",
            arguments = listOf(anyOfValidators.size),
            causes = anyOfReport.errors
        )

    return parentReport.isValid
  }
}
