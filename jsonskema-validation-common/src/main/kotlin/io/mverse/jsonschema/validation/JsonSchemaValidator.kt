package io.mverse.jsonschema.validation

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.validation.factory.KeywordValidatorCreator
import io.mverse.jsonschema.validation.factory.KeywordValidatorCreators
import io.mverse.jsonschema.validation.keywords.KeywordValidator
import lang.collection.ListMultimap
import lang.collection.MutableListMultimap
import lang.json.JsrType
import lang.json.JsrType.ARRAY
import lang.json.JsrType.BOOLEAN
import lang.json.JsrType.NULL
import lang.json.JsrType.NUMBER
import lang.json.JsrType.OBJECT
import lang.json.JsrType.STRING
import lang.json.JsrType.values

/**
 * Main validation processing controller.  There will be a single instance of this class for each json-schema, but each
 * keyword is broken out into a separate processor.
 */
data class JsonSchemaValidator(val factories: KeywordValidatorCreators,
                               /**
                                * The underlying schema being validated.  This instance isn't actually used for validation, it's primarily
                                * here for metadata when recording validation.
                                */
                               override val schema: Schema,
                               val validatorFactory: SchemaValidatorFactoryImpl) : SchemaValidator {

  private val arrayValidators: List<KeywordValidator<*>>
  private val objectValidators: List<KeywordValidator<*>>
  private val numberValidators: List<KeywordValidator<*>>
  private val stringValidators: List<KeywordValidator<*>>
  private val nullValidators: List<KeywordValidator<*>>
  private val booleanValidators: List<KeywordValidator<*>>

  /**
   * Whether this validation has any validation to perform.
   */
  private val noop: Boolean

  init {
    // Cache the validation to avoid infinite recursion
    validatorFactory.cacheValidator(schema.absoluteURI, this)

    val validators = mapValidatorsToType(schema, validatorFactory, factories)
    this.arrayValidators = validators[ARRAY]
    this.objectValidators = validators[OBJECT]
    this.numberValidators = validators[NUMBER]
    this.stringValidators = validators[STRING]
    this.booleanValidators = validators[BOOLEAN]
    this.nullValidators = validators[NULL]

    this.noop = validators.asMap().isEmpty()
  }

  /**
   * Executes this validation for the provided `subject` and appends any validation to the provided `parentReport`
   * @param subject The JsrValue to be validated against this schema
   * @param parentReport The report to append any validation to
   * @return true if the `subject` passed validation
   */
  override fun validate(subject: JsonValueWithPath, parentReport: ValidationReport): Boolean {
    if (noop) {
      return true
    }

    var childReport: ValidationReport? = null

    val applicableValidators = findValidators(subject)
    if (applicableValidators != null) {
      childReport = parentReport.createChildReport()
      val size = applicableValidators.size
      for (i in 0 until size) {
        applicableValidators[i].validate(subject, childReport)
      }
    }

    if (childReport != null && !childReport.isValid) {
      parentReport.addReport(schema, subject, childReport)
    }
    return childReport == null || childReport.isValid
  }

  /**
   * Finds all [KeywordValidator] that are applicable for the given subject. Certain keywords are only applicable
   * if the subject is of a specific type.
   *
   * @param subject The instance to be validated.
   * @return A list of [KeywordValidator] that apply to the given subject.
   */
  internal fun findValidators(subject: JsonValueWithPath): List<KeywordValidator<*>>? {
    return when (subject.type) {
      ARRAY -> arrayValidators
      OBJECT -> objectValidators
      STRING -> stringValidators
      NUMBER -> numberValidators
      BOOLEAN -> booleanValidators
      NULL -> nullValidators
    }
  }

  /**
   * Internal helper method that sorts out keyword validators based on their applicable type.  This makes it more
   * efficient for us to run only validators that apply to the object being validated.
   *
   * @param schema The schema that is to be validated
   * @param validatorFactory A validatorFactory used to construct new [KeywordValidator] instances
   * @param factories A list of [KeywordValidatorCreator] - these inspect the provided schema, and return the
   * necessary keyword validators based on the schema.
   *
   * @return A [ListMultimap] with the keywords sorted by their applicable types.
   */
  private fun mapValidatorsToType(schema: Schema, validatorFactory: SchemaValidatorFactory,
                                  factories: KeywordValidatorCreators): ListMultimap<JsrType, KeywordValidator<*>> {

    val validatorsByType = MutableListMultimap<JsrType, KeywordValidator<*>>()
    schema.keywords.forEach { (keyword, keywordValue) ->
      factories[keyword].forEach { keywordFactory ->
        val keywordValidator = keywordFactory.invokeUnsafe(keywordValue, schema, validatorFactory)
        if (keywordValidator != null) {
          val applicableTypes = keyword.applicableTypes
          if (applicableTypes.isEmpty()) {
            for (applicableType in values()) {
              validatorsByType.add(applicableType, keywordValidator)
            }
          } else {
            applicableTypes.forEach { applicableType -> validatorsByType.add(applicableType, keywordValidator) }
          }
        }
      }
    }
    return validatorsByType.freeze()
  }
}
