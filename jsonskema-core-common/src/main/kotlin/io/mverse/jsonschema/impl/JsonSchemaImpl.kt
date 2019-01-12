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
import io.mverse.jsonschema.keyword.IdKeyword
import io.mverse.jsonschema.keyword.Keyword
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.Keywords.ADDITIONAL_ITEMS
import io.mverse.jsonschema.keyword.Keywords.ADDITIONAL_PROPERTIES
import io.mverse.jsonschema.keyword.Keywords.CONTAINS
import io.mverse.jsonschema.keyword.Keywords.DEFAULT
import io.mverse.jsonschema.keyword.Keywords.DEFINITIONS
import io.mverse.jsonschema.keyword.Keywords.DEPENDENCIES
import io.mverse.jsonschema.keyword.Keywords.DESCRIPTION
import io.mverse.jsonschema.keyword.Keywords.DOLLAR_ID
import io.mverse.jsonschema.keyword.Keywords.ENUM
import io.mverse.jsonschema.keyword.Keywords.EXAMPLES
import io.mverse.jsonschema.keyword.Keywords.FORMAT
import io.mverse.jsonschema.keyword.Keywords.ID
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
import io.mverse.jsonschema.keyword.Keywords.REF
import io.mverse.jsonschema.keyword.Keywords.REQUIRED
import io.mverse.jsonschema.keyword.Keywords.TITLE
import io.mverse.jsonschema.keyword.Keywords.TYPE
import io.mverse.jsonschema.keyword.Keywords.UNIQUE_ITEMS
import io.mverse.jsonschema.utils.Schemas.nullSchema
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.json
import lang.collection.Multimaps
import lang.collection.SetMultimap
import lang.net.URI
import lang.json.JsonSaver
import lang.json.jsonArrayOf
import lang.logging.Logger

val log: Logger = Logger("JsonSchemaImpl")

abstract class JsonSchemaImpl<D : DraftSchema<D>>(
    override val location: SchemaLocation,
    override val keywords: Map<KeywordInfo<*>, Keyword<*>> = emptyMap(),
    override val extraProperties: Map<String, JsonElement> = emptyMap(),
    override val version: JsonSchemaVersion = JsonSchemaVersion.latest)

  : DraftSchema<D>, KeywordContainer(keywords) {

  // ######################################################
  // ###### Getters for common keywords (draft3-6) ########
  // ######################################################

  override val id: URI? by lazy {
    val kw = keywords[DOLLAR_ID] ?: keywords[ID]
    (kw as? IdKeyword)?.value
  }

  override val schemaURI: URI? get() = version.metaschemaURI
  override val title: String? get() = values[TITLE]
  override val description: String? get() = values[DESCRIPTION]
  override val types: Set<JsonSchemaType> get() = values[TYPE] ?: emptySet()
  override val enumValues: JsonArray? get() = values[ENUM]
  override val defaultValue: JsonElement? get() = values[DEFAULT]
  override val format: String? get() = values[FORMAT]
  override val minLength: Int? get() = values[MIN_LENGTH]?.toInt()
  override val maxLength: Int? get() = values[MAX_LENGTH]?.toInt()
  override val pattern: String? get() = values[PATTERN]
  override val minimum: Number? get() = this[MINIMUM]?.limit
  override val maximum: Number? get() = this[MAXIMUM]?.limit
  override val multipleOf: Number? get() = values[MULTIPLE_OF]
  override val minItems: Int? get() = values[MIN_ITEMS]?.toInt()
  override val maxItems: Int? get() = values[MAX_ITEMS]?.toInt()
  override val properties: Map<String, Schema> get() = values[PROPERTIES] ?: emptyMap()
  override val patternProperties: Map<String, Schema> get() = values[PATTERN_PROPERTIES] ?: emptyMap()
  override val additionalPropertiesSchema: D? get() = values[ADDITIONAL_PROPERTIES]?.toDraftVersion()
  override val requiresUniqueItems: Boolean get() = values[UNIQUE_ITEMS] ?: false

  override val additionalItemsSchema: D? get() = this[ITEMS]?.additionalItemSchema?.toDraftVersion()

  override val propertyDependencies: SetMultimap<String, String>
    get() = this[DEPENDENCIES]?.propertyDependencies ?: Multimaps.emptySetMultimap()

  override val propertySchemaDependencies: Map<String, Schema>
    get() = this[DEPENDENCIES]?.dependencySchemas?.value ?: emptyMap()

  override val allItemSchema: D? get() = this[ITEMS]?.allItemSchema?.toDraftVersion()

  override val itemSchemas: List<Schema> get() = this[ITEMS]?.indexedSchemas ?: emptyList()

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

  override fun toJson(version: JsonSchemaVersion, includeExtraProperties: Boolean): JsonObject {
    return json {
      if (keywords.containsKey(REF)) {
        // Output as ref
        REF.key to keyword(REF)?.value?.toString()
      } else {
        keywords.forEach { (keyword, keywordValue) ->
          if (keyword.applicableVersions.contains(version)) {
            keywordValue.toJson(keyword, this, version, includeExtraProperties)
          } else {
            log.warn("Keyword ${keyword.key} does not apply to version: [$version], only for ${keyword.applicableVersions}")
          }
        }
        if (includeExtraProperties) {
          extraProperties.forEach { (prop,value)->
            prop to value
          }
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
    return this as? Draft4Schema ?: Draft4SchemaImpl(this)
  }

  override fun toString(): String = toString(version)

  override fun toString(version: JsonSchemaVersion, includeExtraProperties: Boolean): String {
    return JsonSaver().serialize(this.toJson(version, includeExtraProperties))
  }

  override fun toBuilder(): SchemaBuilder {
    return JsonSchemaBuilder(fromSchema = this)
  }

  override fun  toBuilder(id: URI): SchemaBuilder {
    return JsonSchemaBuilder(fromSchema = this, id = id)
  }

  // ##################################################################
  // ###### Helper methods for subclasses (accessing keywords) ########
  // ##################################################################

  protected open val examples: JsonArray get() = values[EXAMPLES] ?: jsonArrayOf()
  protected open val definitions: Map<String, Schema> get() = values[DEFINITIONS] ?: emptyMap()
  protected open val notSchema: Schema? get() =  values[Keywords.NOT]
  protected open val constValue: JsonElement? get() = values[Keywords.CONST]
  protected open val allOfSchemas: List<Schema> get() = values[Keywords.ALL_OF] ?: emptyList()
  protected open val anyOfSchemas: List<Schema> get() = values[Keywords.ANY_OF] ?: emptyList()
  protected open val oneOfSchemas: List<Schema> get() = values[Keywords.ONE_OF] ?: emptyList()
  protected open val maxProperties: Int? get() = values[MAX_PROPERTIES]?.toInt()
  protected open val minProperties: Int? get() = values[MIN_PROPERTIES]?.toInt()
  protected open val requiredProperties: Set<String> get() = values[REQUIRED] ?: emptySet()
  protected open val exclusiveMinimum: Number? get() = this[MINIMUM]?.exclusiveLimit
  protected open val exclusiveMaximum: Number? get() = this[MAXIMUM]?.exclusiveLimit
  protected open val containsSchema: Schema? get() = values[CONTAINS]
  protected open val propertyNameSchema: Schema? get() = values[PROPERTY_NAMES]

  protected open val isAllowAdditionalItems: Boolean
    get() = this[ADDITIONAL_ITEMS]?.additionalItemSchema != nullSchema

  protected open val isAllowAdditionalProperties: Boolean
  get() = values[ADDITIONAL_PROPERTIES] != nullSchema

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

