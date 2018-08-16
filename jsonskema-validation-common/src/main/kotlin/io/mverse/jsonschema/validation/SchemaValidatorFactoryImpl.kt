package io.mverse.jsonschema.validation

import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.enums.FormatType
import io.mverse.jsonschema.keyword.JsonSchemaKeyword
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.utils.Schemas.falseSchema
import io.mverse.jsonschema.utils.Schemas.nullSchema
import io.mverse.jsonschema.validation.factory.KeywordValidatorCreator
import io.mverse.jsonschema.validation.factory.KeywordValidatorCreators
import io.mverse.jsonschema.validation.keywords.AdditionalPropertiesValidator
import io.mverse.jsonschema.validation.keywords.AllOfValidator
import io.mverse.jsonschema.validation.keywords.AnyOfValidator
import io.mverse.jsonschema.validation.keywords.ConstValidator
import io.mverse.jsonschema.validation.keywords.EnumValidator
import io.mverse.jsonschema.validation.keywords.KeywordValidator
import io.mverse.jsonschema.validation.keywords.LogicValidator
import io.mverse.jsonschema.validation.keywords.NotKeywordValidator
import io.mverse.jsonschema.validation.keywords.OneOfValidator
import io.mverse.jsonschema.validation.keywords.TypeValidator
import io.mverse.jsonschema.validation.keywords.`object`.DependenciesValidator
import io.mverse.jsonschema.validation.keywords.`object`.MaxPropertiesValidator
import io.mverse.jsonschema.validation.keywords.`object`.MinPropertiesValidator
import io.mverse.jsonschema.validation.keywords.`object`.PatternPropertiesValidator
import io.mverse.jsonschema.validation.keywords.`object`.PropertyNameValidator
import io.mverse.jsonschema.validation.keywords.`object`.PropertySchemaValidator
import io.mverse.jsonschema.validation.keywords.`object`.RequiredPropertyValidator
import io.mverse.jsonschema.validation.keywords.array.ArrayContainsValidator
import io.mverse.jsonschema.validation.keywords.array.ArrayItemsValidator
import io.mverse.jsonschema.validation.keywords.array.ArrayMaxItemsValidator
import io.mverse.jsonschema.validation.keywords.array.ArrayMinItemsValidator
import io.mverse.jsonschema.validation.keywords.array.ArrayUniqueItemsValidator
import io.mverse.jsonschema.validation.keywords.number.NumberLimitValidators
import io.mverse.jsonschema.validation.keywords.number.NumberMultipleOfValidator
import io.mverse.jsonschema.validation.keywords.string.StringFormatValidator
import io.mverse.jsonschema.validation.keywords.string.StringMaxLengthValidator
import io.mverse.jsonschema.validation.keywords.string.StringMinLengthValidator
import io.mverse.jsonschema.validation.keywords.string.StringPatternValidator
import io.mverse.jsonschema.validation.keywords.string.formatValidators.ColorFormatValidator
import io.mverse.jsonschema.validation.keywords.string.formatValidators.DateFormatValidator
import io.mverse.jsonschema.validation.keywords.string.formatValidators.DateTimeFormatValidator
import io.mverse.jsonschema.validation.keywords.string.formatValidators.EmailFormatValidator
import io.mverse.jsonschema.validation.keywords.string.formatValidators.HostnameFormatValidator
import io.mverse.jsonschema.validation.keywords.string.formatValidators.IDNEmailFormatValidator
import io.mverse.jsonschema.validation.keywords.string.formatValidators.IDNHostnameFormatValidator
import io.mverse.jsonschema.validation.keywords.string.formatValidators.IPV4Validator
import io.mverse.jsonschema.validation.keywords.string.formatValidators.IPV6Validator
import io.mverse.jsonschema.validation.keywords.string.formatValidators.IRIReferenceFormatValidator
import io.mverse.jsonschema.validation.keywords.string.formatValidators.JsonPointerValidator
import io.mverse.jsonschema.validation.keywords.string.formatValidators.NoopFormatValidator
import io.mverse.jsonschema.validation.keywords.string.formatValidators.PatternBasedValidator
import io.mverse.jsonschema.validation.keywords.string.formatValidators.PhoneFormatValidator
import io.mverse.jsonschema.validation.keywords.string.formatValidators.RegexFormatValidator
import io.mverse.jsonschema.validation.keywords.string.formatValidators.TimeFormatValidator
import io.mverse.jsonschema.validation.keywords.string.formatValidators.URIFormatValidator
import io.mverse.jsonschema.validation.keywords.string.formatValidators.URIReferenceFormatValidator
import io.mverse.jsonschema.validation.keywords.string.formatValidators.URITemplateFormatValidator
import lang.MutableSetMultimap
import lang.Pattern
import lang.URI
import lang.isAbsolute

class SchemaValidatorFactoryImpl(private val validatorCache: MutableMap<URI, SchemaValidator> = hashMapOf(),
                                 private val customFormatValidators: Map<String, FormatValidator>,
                                 private val validators: KeywordValidatorCreators) : SchemaValidatorFactory {

  internal fun cacheValidator(schemaURI: URI, validator: SchemaValidator) {
    if (schemaURI.isAbsolute) {
      validatorCache.getOrPut(schemaURI) { validator }
    }
  }

  fun createValidators(schemas: List<Schema>): List<SchemaValidator> = schemas.map { this.createValidator(it) }

  override fun createValidator(schema: Schema): SchemaValidator {
    if (nullSchema == schema) {
      return NullSchemaValidator.instance
    } else if (falseSchema == schema) {
      return FalseSchemaValidator.instance
    }

    val schemaURI = schema.location.uniqueURI
    val cachedValue = validatorCache[schemaURI]
    return if (cachedValue != null) cachedValue
    else {
      val validator: SchemaValidator
      validator = JsonSchemaValidator(
          factories = validators,
          schema = schema,
          validatorFactory = this)
      this.cacheValidator(schemaURI, validator)
      validator
    }
  }

  override fun getFormatValidator(input: String): FormatValidator? = customFormatValidators[input]



  companion object {

    val DEFAULT_VALIDATOR_FACTORY: SchemaValidatorFactory = SchemaValidatorFactoryBuilder().build()

    fun createValidatorForSchema(schema: Schema): SchemaValidator {
      return DEFAULT_VALIDATOR_FACTORY.createValidator(schema)
    }

    fun builder(): SchemaValidatorFactoryBuilder {
      return SchemaValidatorFactoryBuilder()
    }

    /**
     * Static factory method for `FormatValidator` implementations supporting the
     * `getFormatName`s mandated by the json schema spec.
     *
     *
     *
     *  * date-time
     *  * email
     *  * hostname
     *  * uri
     *  * ipv4
     *  * ipv6
     *
     *
     * @param format one of the 6 built-in formats.
     * @return a `FormatValidator` implementation handling the `getFormatName` format.
     */
    internal fun forFormat(format: FormatType): FormatValidator {
      val formatName = format.toString()
      when (formatName) {
        "date-time" -> return DateTimeFormatValidator()
        "time" -> return TimeFormatValidator()
        "date" -> return DateFormatValidator()
        "email" -> return EmailFormatValidator()
        "idn-email" -> return IDNEmailFormatValidator()
        "hostname" -> return HostnameFormatValidator()
        "idn-hostname" -> return IDNHostnameFormatValidator()
        "host-name" -> return HostnameFormatValidator()
        "uri" -> return URIFormatValidator()
        "iri" -> return IRIReferenceFormatValidator()
        "ipv4" -> return IPV4Validator()
        "ip-address" -> return IPV4Validator()
        "ipv6" -> return IPV6Validator()
        "json-pointer" -> return JsonPointerValidator()
        "relative-json-pointer" -> return JsonPointerValidator()
        "uri-template" -> return URITemplateFormatValidator()
        "uri-reference" -> return URIReferenceFormatValidator()
        "iri-reference" -> return IRIReferenceFormatValidator()
        "uriref" -> return URIReferenceFormatValidator()
        "style" -> return NoopFormatValidator("style")
        "color" -> return ColorFormatValidator()
        "phone" -> return PhoneFormatValidator()
        "regex" -> return RegexFormatValidator()
        "utc-millisec" -> return PatternBasedValidator(Pattern("^[0-9]+$"), "utc-millisex")
        else -> throw IllegalArgumentException("unsupported format: $formatName")
      }
    }
  }
}

class SchemaValidatorFactoryBuilder {

  private val customFormatValidators = hashMapOf<String, FormatValidator>()
  val factories = MutableSetMultimap<KeywordInfo<*>, KeywordValidatorCreator<*, *>>()

  init {
    withCommonValidators()
    initCoreFormatValidators()
  }

  /*
   * todo: Find a better way to do this in kotlin.  The main reason for the generics is enforcing that
   * the validator being created works for the keyword it thinks its for
   */
  inline fun <reified K : JsonSchemaKeyword<*>, reified V : KeywordValidator<K>> addValidator(keyword: KeywordInfo<K>, noinline creator:(K, Schema, SchemaValidatorFactory) -> V?): SchemaValidatorFactoryBuilder {
    factories.add(keyword, KeywordValidatorCreator(creator))
    return this
  }

  fun build(): SchemaValidatorFactoryImpl {
    return SchemaValidatorFactoryImpl(customFormatValidators = this.customFormatValidators,
        validators = KeywordValidatorCreators(this.factories))
  }

  fun addCustomFormatValidator(format: String, formatValidator: FormatValidator): SchemaValidatorFactoryBuilder {
    check(format.isNotBlank()) { "format must not be blank" }

    this.customFormatValidators[format] = formatValidator
    return this
  }

  fun addCustomFormatValidator(format: String, formatValidator: (String)->String?): SchemaValidatorFactoryBuilder {
    return addCustomFormatValidator(format, object:FormatValidator {
      override fun validate(subject: String): String? = formatValidator(subject)
    })
  }

  fun withCommonValidators(): SchemaValidatorFactoryBuilder {

    // ########################################################### //
    // #########  COMMON VALIDATORS      ######################### //
    // ########################################################### //

    this.addValidator(Keywords.TYPE) { keyword, schema, factory -> TypeValidator(keyword, schema, factory) }
    this.addValidator(Keywords.ENUM) { keyword, schema, factory -> EnumValidator(keyword, schema, factory) }
    this.addValidator(Keywords.NOT) { keyword, schema, factory -> NotKeywordValidator(keyword, schema, factory) }
    this.addValidator(Keywords.CONST) { keyword, parentSchema, factory -> ConstValidator(keyword, parentSchema, factory) }
    this.addValidator(Keywords.ALL_OF) { keyword, schema, factory -> AllOfValidator(keyword, schema, factory) }
    this.addValidator(Keywords.ANY_OF) { keyword, schema, factory -> AnyOfValidator(keyword, schema, factory) }
    this.addValidator(Keywords.ONE_OF) { keyword, schema, factory -> OneOfValidator(keyword, schema, factory) }
    this.addValidator(Keywords.IF) { keyword, schema, factory -> LogicValidator(keyword, schema, factory) }

    // ########################################################### //
    // #########  STRING VALIDATORS      ######################### //
    // ########################################################### //
    this.addValidator(Keywords.MAX_LENGTH) { keyword, schema, _ -> StringMaxLengthValidator(keyword, schema) }
    this.addValidator(Keywords.MIN_LENGTH) { keyword, schema, _ -> StringMinLengthValidator(keyword, schema) }
    this.addValidator(Keywords.PATTERN) { keyword, schema, _ -> StringPatternValidator(keyword, schema) }
    this.addValidator(Keywords.FORMAT) { keyword, schema, factory -> StringFormatValidator(keyword, schema, factory) }

    // ########################################################### //
    // #########  ARRAY VALIDATORS      ######################### //
    // ########################################################### //

    this.addValidator(Keywords.MAX_ITEMS) { number, schema, factory -> ArrayMaxItemsValidator(number, schema, factory) }
    this.addValidator(Keywords.MIN_ITEMS) { number, schema, factory -> ArrayMinItemsValidator(number, schema, factory) }
    this.addValidator(Keywords.UNIQUE_ITEMS) { keyword, schema, factory -> ArrayUniqueItemsValidator(keyword, schema, factory) }
    this.addValidator(Keywords.ITEMS) { keyword, schema, factory -> ArrayItemsValidator.getArrayItemsValidator(keyword, schema, factory) }
    this.addValidator(Keywords.CONTAINS) { keyword, schema, factory -> ArrayContainsValidator(keyword, schema, factory) }

    // ########################################################### //
    // #########  SCHEMA_NUMBER VALIDATORS      ######################### //
    // ########################################################### //
    this.addValidator(Keywords.MINIMUM) { keyword, schema, _ -> NumberLimitValidators.getMinValidator(keyword, schema) }
    this.addValidator(Keywords.MAXIMUM) { keyword, schema, _ -> NumberLimitValidators.getMaxValidator(keyword, schema) }
    this.addValidator(Keywords.MULTIPLE_OF) { numberKeyword, schema, _ -> NumberMultipleOfValidator(numberKeyword, schema) }

    // ########################################################### //
    // #########  OBJECT VALIDATORS      ######################### //
    // ########################################################### //
    this.addValidator(Keywords.PROPERTIES) { keyword, schema, factory -> PropertySchemaValidator(keyword, schema, factory) }
    this.addValidator(Keywords.PROPERTY_NAMES) { keyword, schema, factory -> PropertyNameValidator(keyword, schema, factory) }
    this.addValidator(Keywords.REQUIRED) { keyword, schema, _ -> RequiredPropertyValidator(keyword, schema) }
    this.addValidator(Keywords.PATTERN_PROPERTIES) { keyword, schema, factory -> PatternPropertiesValidator(keyword, schema, factory) }
    this.addValidator(Keywords.ADDITIONAL_PROPERTIES) { keyword, schema, factory -> AdditionalPropertiesValidator(keyword, schema, factory) }
    this.addValidator(Keywords.DEPENDENCIES) { keyword, schema, factory -> DependenciesValidator(keyword, schema, factory) }
    this.addValidator(Keywords.MIN_PROPERTIES) { number, schema, _ -> MinPropertiesValidator(number, schema) }
    this.addValidator(Keywords.MAX_PROPERTIES) { keyword, schema, _ -> MaxPropertiesValidator(keyword, schema) }

    return this
  }

  private fun initCoreFormatValidators() {
    for (formatType in FormatType.values()) {
      customFormatValidators[formatType.toString()] = SchemaValidatorFactoryImpl.forFormat(formatType)
    }
  }
}
