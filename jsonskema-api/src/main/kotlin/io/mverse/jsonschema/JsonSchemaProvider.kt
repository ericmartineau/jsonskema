//package io.mverse.jsonschema
//
//import com.google.common.base.Preconditions.checkNotNull
//import io.mverse.jsonschema.JsonValueWithPath.fromJsonValue
//import java.lang.String.format
//
//import io.mverse.jsonschema.enums.JsonSchemaVersion
//import io.mverse.jsonschema.loading.SchemaLoader
//import io.mverse.jsonschema.loading.SchemaLoadingException
//import io.mverse.jsonschema.loading.SchemaReader
//import io.mverse.jsonschema.validation.SchemaValidator
//import io.mverse.jsonschema.validation.SchemaValidatorFactory
//import io.mverse.jsonschema.validation.ValidationReport
//import java.io.InputStream
//import java.lang.reflect.Constructor
//import lang.URI
//import java.util.ServiceLoader
//import lang.json.JsonObject
//import lang.json.JsonValue
//import kotlin.reflect.KClass
//
///**
// * This class provides the simplest way into the validation and loading APIs.
// */
//object JsonSchemaProvider {
//  private val DEFAULT_VALIDATOR_FACTORY = "io.mverse.jsonschema.validation.SchemaValidatorFactoryImpl"
//  private val DEFAULT_LOADER = "io.mverse.jsonschema.loading.SchemaLoaderImpl"
//  private val DEFAULT_READER = "io.mverse.jsonschema.loading.SchemaLoaderImpl"
//  private val DEFAULT_BUILDER = "io.mverse.jsonschema.builder.JsonSchemaBuilder"
//
//  private var validatorFactory: SchemaValidatorFactory? = null
//  private var schemaLoader: SchemaLoader? = null
//  private var schemaReader: SchemaReader? = null
//
//  // ##################################################################
//  // ########  CONVENIENCE ENTRY POINT METHODS ########################
//  // ##################################################################
//
//  /**
//   * Returns a new instance of a schemaBuilder.  If you want to assign an $id to your schema, use one
//   * of the other methods, [.schemaBuilder] or [.schemaBuilder]
//   */
//  @Suppress("unchecked_cast")
//  fun <B : SchemaBuilder<B>> schemaBuilder(): B {
//    return createComponent(DEFAULT_BUILDER, SchemaBuilder<*>::class) as B
//  }
//
//  /**
//   * Creates a new instance of a schemaBuilder, using a provided URI as the $id of the schema.
//   */
//  fun schemaBuilder(id: URI): SchemaBuilder<*> {
//    return createComponent(DEFAULT_BUILDER, SchemaBuilder<*>::class, id)
//  }
//
//  /**
//   * Creates a new instance of a schemaBuilder, using a provided String as the $id of the schema.
//   */
//  fun schemaBuilder(`$schema`: JsonSchemaVersion, id: String): SchemaBuilder<*> {
//    return createComponent(DEFAULT_BUILDER, SchemaBuilder<*>::class, `$schema`, URI.create(id))
//  }
//
//  fun schemaBuilder(id: String): SchemaBuilder<*> {
//    return createComponent(DEFAULT_BUILDER, SchemaBuilder<*>::class, URI.create(id))
//  }
//
//  fun getValidator(schema: Schema): SchemaValidator {
//    return validatorFactory()!!.createValidator(schema)
//  }
//
//  /**
//   * Reads a schema from a [JsonObject] document.  This method will cache the resulting schema for future use.
//   *
//   *
//   * If you want to control the cache or bypass the cache, then use the [.createSchemaReader], as each invocation
//   * creates a brand new instance of the loader with a fresh cache.
//   *
//   * @param jsonObject The document to load the schema from.
//   * @return The loaded schema instance
//   * @throws SchemaLoadingException It's unchecked, so more for documentation
//   */
//  @Throws(SchemaLoadingException::class)
//  fun readSchema(jsonObject: String): Schema {
//    return schemaReader()!!.readSchema(jsonObject)
//  }
//
//  /**
//   * Reads a schema from a [JsonObject] document.  This method will cache the resulting schema for future use.
//   *
//   *
//   * If you want to control the cache or bypass the cache, then use the [.createSchemaReader], as each invocation
//   * creates a brand new instance of the loader with a fresh cache.
//   *
//   * @param jsonObject The document to load the schema from.
//   * @return The loaded schema instance
//   * @throws SchemaLoadingException It's unchecked, so more for documentation
//   */
//  @Throws(SchemaLoadingException::class)
//  fun readSchema(jsonObject: InputStream): Schema {
//    return schemaReader()!!.readSchema(jsonObject)
//  }
//
//  /**
//   * Validates the provided jsonValue against a schema.  This method will cache the validator for future use.  If
//   * you don't want to cache the validator, use the [.createValidatorFactory] method to get a fresh
//   * instance each time.
//   * @param schema The schema to validate against
//   * @param value The value being validated
//   * @return The validation report
//   */
//  fun validateSchema(schema: Schema, value: JsonValue): ValidationReport {
//    checkNotNull(value, "value must not be null")
//    checkNotNull(schema, "schema must not be null")
//
//    val validator = validatorFactory()!!.createValidator(schema)
//    val jsonValue = fromJsonValue(value, value, schema.location)
//    val report = ValidationReport()
//    validator.validate(jsonValue, report)
//    return report
//  }
//
//  // ##################################################################
//  // ########  FACTORIES: CREATE NEW INSTANCES EACH TIME ##############
//  // ##################################################################
//
//  fun createSchemaLoader(): SchemaLoader {
//    return createComponent(DEFAULT_LOADER, SchemaLoader::class.java)
//  }
//
//  fun createSchemaReader(): SchemaReader {
//    return createComponent(DEFAULT_READER, SchemaReader::class.java)
//  }
//
//  fun createValidatorFactory(): SchemaValidatorFactory {
//    return createComponent(DEFAULT_VALIDATOR_FACTORY, SchemaValidatorFactory::class.java)
//  }
//
//  // ##################################################################
//  // ########  LAZY GETTER METHODS  ###################################
//  // ##################################################################
//
//  private fun schemaLoader(): SchemaLoader? {
//    if (schemaLoader == null) {
//      schemaLoader = createSchemaLoader()
//    }
//    return schemaLoader
//  }
//
//  private fun validatorFactory(): SchemaValidatorFactory? {
//    if (validatorFactory == null) {
//      validatorFactory = createValidatorFactory()
//    }
//    return validatorFactory
//  }
//
//  private fun schemaReader(): SchemaReader? {
//    if (schemaReader == null) {
//      schemaReader = createSchemaReader()
//    }
//    return schemaReader
//  }
//
//  private fun <X> createComponent(defaultImpl: String, type: Class<X>): X {
//    val loader = ServiceLoader.load(type)
//    val it = loader.iterator()
//    if (it.hasNext()) {
//      return it.next()
//    }
//    try {
//      @SuppressWarnings("unchecked")
//      val clazz = Class.forName(defaultImpl) as Class<X>
//      return clazz.newInstance()
//    } catch (x: ClassNotFoundException) {
//      throw IllegalStateException(format("Provider [%s] not found.  Make sure you include any appropriate modules", defaultImpl), x)
//    } catch (x: Exception) {
//      throw IllegalStateException(format("Provider [%s] not instantiated", defaultImpl), x)
//    }
//  }
//
//  private fun <X:Any> createComponent(defaultImpl: String, type: KClass<X>, vararg input: Any): X {
//    val loader = ServiceLoader.load(type)
//    val it = loader.iterator()
//    if (it.hasNext()) {
//      return it.next()
//    }
//    try {
//      val argTypes = arrayOfNulls<Class<*>>(input.size)
//      var i = 0
//      for (arg in input) {
//        argTypes[i++] = arg.getClass()
//      }
//      @Suppress("unchecked_cast")
//      val clazz = Class.forName(defaultImpl) as Class<X>
//      val constructor = clazz.getDeclaredConstructor(argTypes)
//      return constructor.newInstance(input)
//    } catch (x: ClassNotFoundException) {
//      throw IllegalStateException(format("Provider [%s] not found.  Make sure you include any appropriate modules", defaultImpl), x)
//    } catch (x: Exception) {
//      throw IllegalStateException(format("Provider [%s] not instantiated", defaultImpl), x)
//    }
//  }
//}
