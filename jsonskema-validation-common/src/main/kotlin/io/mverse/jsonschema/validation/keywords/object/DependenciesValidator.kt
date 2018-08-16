package io.mverse.jsonschema.validation.keywords.`object`

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.DependenciesKeyword
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.validation.SchemaValidator
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import io.mverse.jsonschema.validation.ValidationReport
import io.mverse.jsonschema.validation.keywords.KeywordValidator
import lang.SetMultimap

class DependenciesValidator(keyword: DependenciesKeyword, schema: Schema, factory: SchemaValidatorFactory) : KeywordValidator<DependenciesKeyword>(Keywords.DEPENDENCIES, schema) {


  private val dependencyValidators: Map<String, SchemaValidator> = keyword.dependencySchemas.value
      .entries.map { it.key to factory.createValidator(it.value) }
      .toMap()

  private val propertyDependencies: SetMultimap<String, String> = keyword.propertyDependencies

  override fun validate(subject: JsonValueWithPath, parentReport: ValidationReport): Boolean {
    for ((propName, dependencyValidator) in dependencyValidators) {
      if (subject.containsKey(propName)) {
        dependencyValidator.validate(subject, parentReport)
      }
    }

    propertyDependencies.asMap().forEach { (ifThisPropertyExists, set)->
      for (thenThisMustAlsoExist in set) {
        if (subject.containsKey(ifThisPropertyExists) && !subject.containsKey(thenThisMustAlsoExist)) {
          parentReport += buildKeywordFailure(subject)
              .withError("property [%s] is required because [%s] was present", thenThisMustAlsoExist, ifThisPropertyExists)

        }
      }
    }
    return parentReport.isValid
  }
}
