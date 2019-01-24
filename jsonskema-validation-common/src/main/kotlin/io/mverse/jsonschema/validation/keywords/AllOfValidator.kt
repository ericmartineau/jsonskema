package io.mverse.jsonschema.validation.keywords

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.SchemaListKeyword
import io.mverse.jsonschema.validation.SchemaValidator
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import io.mverse.jsonschema.validation.ValidationReport

class AllOfValidator(keyword: SchemaListKeyword, schema: Schema, factory: SchemaValidatorFactory) : KeywordValidator<SchemaListKeyword>(Keywords.ALL_OF, schema) {

  private val allOfValidators: List<SchemaValidator> = keyword.subschemas
      .map { factory.createValidator(it) }

  override fun validate(subject: JsonValueWithPath, parentReport: ValidationReport): Boolean {
    val report = parentReport.createChildReport()
    for (validator in allOfValidators) {
      validator.validate(subject, report)
    }

    val failures = report.errors

    val matchingCount = allOfValidators.size - failures.size
    val subschemaCount = allOfValidators.size

    if (matchingCount < subschemaCount) {
      parentReport += this.buildKeywordFailure(subject)
          .copy(errorMessage = "only %d subschema matches out of %d",
              arguments = listOf(matchingCount, subschemaCount),
              causes = failures)
    }

    return parentReport.isValid
  }
}
