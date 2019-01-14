package io.mverse.jsonschema.validation.keywords

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.Keywords.ONE_OF
import io.mverse.jsonschema.keyword.SchemaListKeyword
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import io.mverse.jsonschema.validation.ValidationReport

class OneOfValidator(keyword: SchemaListKeyword, schema: Schema, factory: SchemaValidatorFactory)
  : KeywordValidator<SchemaListKeyword>(ONE_OF, schema) {

  private val oneOfValidators = keyword.subschemas.map { factory.createValidator(it) }

  override fun validate(subject: JsonValueWithPath, parentReport: ValidationReport): Boolean {
    val report = parentReport.createChildReport()
    for (validator in oneOfValidators) {
      validator.validate(subject, report)
    }

    val failures = report.errors

    val matchingCount = oneOfValidators.size - failures.size

    if (matchingCount != 1) {
      parentReport += buildKeywordFailure(subject)
          .copy(
              errorMessage = "%d subschemas matched instead of one",
              arguments = listOf(matchingCount),
              causes = failures)
    }

    return parentReport.isValid
  }
}
