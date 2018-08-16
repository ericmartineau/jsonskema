package io.mverse.jsonschema.impl

import io.mverse.jsonschema.Draft3Schema
import io.mverse.jsonschema.Draft4Schema
import io.mverse.jsonschema.Draft6Schema
import io.mverse.jsonschema.Draft7Schema
import io.mverse.jsonschema.DraftSchema
import io.mverse.jsonschema.KeywordContainer
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaBuilder
import io.mverse.jsonschema.SchemaLocation
import io.mverse.jsonschema.builder.JsonSchemaBuilder
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.keyword.JsonSchemaKeyword
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.Keywords.ADDITIONAL_ITEMS
import io.mverse.jsonschema.keyword.Keywords.ADDITIONAL_PROPERTIES
import io.mverse.jsonschema.keyword.Keywords.CONTAINS
import io.mverse.jsonschema.keyword.Keywords.DEPENDENCIES
import io.mverse.jsonschema.keyword.Keywords.ITEMS
import io.mverse.jsonschema.keyword.Keywords.MAXIMUM
import io.mverse.jsonschema.keyword.Keywords.MAX_ITEMS
import io.mverse.jsonschema.keyword.Keywords.MAX_LENGTH
import io.mverse.jsonschema.keyword.Keywords.MAX_PROPERTIES
import io.mverse.jsonschema.keyword.Keywords.MINIMUM
import io.mverse.jsonschema.keyword.Keywords.MIN_ITEMS
import io.mverse.jsonschema.keyword.Keywords.MIN_LENGTH
import io.mverse.jsonschema.keyword.Keywords.MIN_PROPERTIES
import io.mverse.jsonschema.keyword.Keywords.MULTIPLE_OF
import io.mverse.jsonschema.keyword.Keywords.PATTERN
import io.mverse.jsonschema.keyword.Keywords.PATTERN_PROPERTIES
import io.mverse.jsonschema.keyword.Keywords.PROPERTIES
import io.mverse.jsonschema.keyword.Keywords.PROPERTY_NAMES
import io.mverse.jsonschema.keyword.Keywords.REQUIRED
import io.mverse.jsonschema.keyword.Keywords.UNIQUE_ITEMS
import io.mverse.jsonschema.utils.Schemas.nullSchema
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.json
import lang.Logger
import lang.Multimaps
import lang.SetMultimap
import lang.URI
import lang.json.JsonSaver

val log: Logger = Logger("JsonSchemaImpl")

abstract class JsonSchemaImpl<D : DraftSchema<D>>(
    override val location: SchemaLocation,
    override val keywords: Map<KeywordInfo<*>, JsonSchemaKeyword<*>> = emptyMap(),
    override val extraProperties: Map<String, JsonElement> = emptyMap(),
    override val version: JsonSchemaVersion = JsonSchemaVersion.latest())

  : DraftSchema<D>, KeywordContainer(keywords) {

  // ######################################################
  // ###### Getters for common keywords (draft3-6) ########
  // ######################################################

  override val id: URI? by keywords(Keywords.DOLLAR_ID)
  override val schemaURI: URI? get() = version.metaschemaURI
  override val title: String? by keywords(Keywords.TITLE)
  override val description: String? by keywords(Keywords.DESCRIPTION)
  override val types: Set<JsonSchemaType> by keywords(Keywords.TYPE, emptySet())
  override val enumValues: kotlinx.serialization.json.JsonArray? by keywords(Keywords.ENUM)
  override val defaultValue: JsonElement? by keywords(Keywords.DEFAULT)
  override val format: String? by keywords(Keywords.FORMAT)
  override val minLength: Int? by numberKeyword(MIN_LENGTH)
  override val maxLength: Int? by numberKeyword(MAX_LENGTH)
  override val pattern: String? by keywords(PATTERN)
  override val minimum: Number? by lazy { keyword(MINIMUM)?.limit }
  override val maximum: Number? by lazy { keyword(MAXIMUM)?.limit }
  override val multipleOf: Number? by numberKeyword(MULTIPLE_OF)
  override val minItems: Int? by numberKeyword(MIN_ITEMS)
  override val maxItems: Int? by numberKeyword(MAX_ITEMS)
  override val properties: Map<String, Schema> by keywords(PROPERTIES, emptyMap())
  override val patternProperties: Map<String, Schema> by keywords(PATTERN_PROPERTIES, emptyMap())
  override val additionalPropertiesSchema: D? by lazy {keyword(ADDITIONAL_PROPERTIES)?.value?.toDraftVersion()}
  override val requiresUniqueItems: Boolean by keywords(UNIQUE_ITEMS, false)

  override val additionalItemsSchema: D? by lazy {
    keyword(ITEMS)?.additionalItemSchema?.toDraftVersion()
  }

  override val propertyDependencies: SetMultimap<String, String> by lazy {
    keyword(DEPENDENCIES)?.propertyDependencies ?: Multimaps.emptySetMultimap()
  }

  override val propertySchemaDependencies: Map<String, Schema> by lazy {
    keyword(DEPENDENCIES)?.dependencySchemas?.value ?: emptyMap()
  }

  override val allItemSchema: D? by lazy {
    keyword(Keywords.ITEMS)?.allItemSchema?.toDraftVersion()
  }

  override val itemSchemas: List<Schema> by lazy {
    keyword(Keywords.ITEMS)?.indexedSchemas ?: emptyList()
  }

  private fun Schema.toDraftVersion(): D {
    return convertVersion(this)
  }

  protected constructor(from: Schema, version: JsonSchemaVersion) :
      this(location = from.location,
          keywords = from.keywords,
          extraProperties = from.extraProperties,
          version = version)

  // ######################################################
  // ###### Base Schema Methods Implemented  ##############
  // ######################################################

  override fun toJson(version: JsonSchemaVersion): JsonObject {
    return json {
      keywords.forEach { (keyword, keywordValue) ->
        if (keyword.applicableVersions.contains(version)) {
          keywordValue.toJson(keyword, this, version)
        } else {
          log.warn("Keyword ${keyword.key} does not apply to version: [$version], only for ${keyword.applicableVersions}")
        }
      }
    }
  }

  // ######################################################
  // ###### asDraft* for getting a different version ######
  // ######################################################

  override fun asDraft6(): Draft6Schema {
    return if (this is Draft6Schema) this
    else Draft6SchemaImpl(this)
  }

  override fun asDraft3(): Draft3Schema {
    return this as? Draft3Schema ?: Draft3SchemaImpl(this)
  }

  override fun asDraft7(): Draft7Schema {
    return this as? Draft7Schema ?: Draft7SchemaImpl(this)
  }

  override fun asDraft4(): Draft4Schema {
    return this as? Draft4Schema ?:  Draft4SchemaImpl(this)
  }

  override fun toString(): String = toString(version)

  override fun toString(version: JsonSchemaVersion): String {
    return JsonSaver().serialize(this.toJson(version))
  }

  override fun <X : SchemaBuilder<X>> toBuilder(): X {
    @Suppress("unchecked_cast")
    return JsonSchemaBuilder(fromSchema = this) as X
  }

  override fun <X : SchemaBuilder<X>> toBuilder(id: URI): X {
    @Suppress("unchecked_cast")
    return JsonSchemaBuilder(fromSchema = this, id = id) as X
  }

  // ##################################################################
  // ###### Helper methods for subclasses (accessing keywords) ########
  // ##################################################################

  protected fun keywords(): Map<KeywordInfo<*>, JsonSchemaKeyword<*>> {
    return keywords
  }

  protected open val examples: kotlinx.serialization.json.JsonArray by keywords(Keywords.EXAMPLES, kotlinx.serialization.json.JsonArray(emptyList()))
  protected open val definitions: Map<String, Schema> by keywords(Keywords.DEFINITIONS, emptyMap())
  protected open val notSchema: Schema? by keywords(Keywords.NOT)
  protected open val constValue: JsonElement? by keywords(Keywords.CONST)
  protected open val allOfSchemas: List<Schema> by keywords(Keywords.ALL_OF, emptyList())
  protected open val anyOfSchemas: List<Schema> by keywords(Keywords.ANY_OF, emptyList())
  protected open val oneOfSchemas: List<Schema> by keywords(Keywords.ONE_OF, emptyList())
  protected open val maxProperties: Int? by numberKeyword(MAX_PROPERTIES)
  protected open val minProperties: Int? by numberKeyword(MIN_PROPERTIES)
  protected open val requiredProperties: Set<String> by keywords(REQUIRED, emptySet())
  protected open val exclusiveMinimum: Number? by lazy { keyword(MINIMUM)?.exclusiveLimit }
  protected open val exclusiveMaximum: Number? by lazy { keyword(MAXIMUM)?.exclusiveLimit }
  protected open val containsSchema: Schema? by keywords(CONTAINS)
  protected open val propertyNameSchema: Schema? by keywords(PROPERTY_NAMES)

  protected open val isAllowAdditionalItems: Boolean by lazy {
    keyword(ADDITIONAL_ITEMS)?.additionalItemSchema != nullSchema
  }

  protected open val isAllowAdditionalProperties: Boolean by lazy {
    keyword(ADDITIONAL_PROPERTIES)?.value != nullSchema
  }

  override fun equals(other: Any?): Boolean {
    return when {
      this === other -> true
      other !is JsonSchemaImpl<*> -> false
      else -> this.keywords == other.keywords
    }
  }

  override fun hashCode(): Int {
    return keywords.hashCode()
  }
}
