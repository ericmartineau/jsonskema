package io.mverse.jsonschema.validation.keywords.array

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.SingleSchemaKeyword
import io.mverse.jsonschema.validation.SchemaValidator
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import io.mverse.jsonschema.validation.ValidationReport
import io.mverse.jsonschema.validation.keywords.KeywordValidator

data class ArrayContainsValidator(val keyword: SingleSchemaKeyword,
                                  override val schema: Schema,
                                  val factory: SchemaValidatorFactory,
                                  private val containsValidator: SchemaValidator = factory.createValidator(keyword.value))
  : KeywordValidator<SingleSchemaKeyword>(Keywords.CONTAINS, schema) {



  override fun validate(subject: JsonValueWithPath, parentReport: ValidationReport): Boolean {
    for (i in 0 until subject.jsonArray!!.size) {
      val trap = parentReport.createChildReport()
      val item = subject[i]
      if (containsValidator.validate(item, trap)) {
        return true
      }
    }

    parentReport += buildKeywordFailure(subject).copy(errorMessage = "array does not contain at least 1 matching item")
    return parentReport.isValid
  }
}
