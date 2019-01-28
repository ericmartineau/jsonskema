package io.mverse.jsonschema.impl

import io.mverse.jsonschema.Draft3Schema
import io.mverse.jsonschema.Draft4Schema
import io.mverse.jsonschema.Draft6Schema
import io.mverse.jsonschema.Draft7Schema
import io.mverse.jsonschema.DraftSchema
import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.KeywordContainer
import io.mverse.jsonschema.RefSchema
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.keyword.Draft3Keywords
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
import io.mverse.jsonschema.keyword.Keywords.ELSE
import io.mverse.jsonschema.keyword.Keywords.ENUM
import io.mverse.jsonschema.keyword.Keywords.EXAMPLES
import io.mverse.jsonschema.keyword.Keywords.FORMAT
import io.mverse.jsonschema.keyword.Keywords.ID
import io.mverse.jsonschema.keyword.Keywords.IF
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
import io.mverse.jsonschema.keyword.Keywords.THEN
import io.mverse.jsonschema.keyword.Keywords.TITLE
import io.mverse.jsonschema.keyword.Keywords.TYPE
import io.mverse.jsonschema.keyword.Keywords.UNIQUE_ITEMS
import io.mverse.jsonschema.utils.Schemas.nullSchema
import io.mverse.logging.mlogger
import lang.collection.Multimaps
import lang.collection.SetMultimap
import lang.collection.freezeList
import lang.collection.freezeMap
import lang.json.JsrArray
import lang.json.JsrObject
import lang.json.JsrValue
import lang.json.jsrArrayOf
import lang.json.jsrJson
import lang.json.jsrObject
import lang.json.writeJson
import lang.net.URI
import kotlin.reflect.KClass

data class DraftSchemaImpl<D : DraftSchema>(
    private val type: KClass<D>,
    override val schema: Schema)
  : Draft3Schema, Draft4Schema, Draft6Schema, Draft7Schema, KeywordContainer({schema.keywords.freezeMap()}) {

  init {
    initialize()
  }

  // ######################################################
  // ###### Getters for common keywords (draft3-6) ########
  // ######################################################

  override val id: URI? by lazy {
    val kw = keywords[DOLLAR_ID] ?: keywords[ID]
    (kw as? IdKeyword)?.value
  }

  override val isRefSchema: Boolean get() = schema is RefSchema || REF in keywords


  override val version: JsonSchemaVersion = schema.version
  override val keywords: Map<KeywordInfo<*>, Keyword<*>> get() = schema.keywords
  override val schemaURI get() = version.metaschemaURI
  override val title: String? get() = values[TITLE]
  override val description: String? get() = values[DESCRIPTION]
  override val types: Set<JsonSchemaType> get() = values[TYPE] ?: emptySet()
  override val enumValues: JsrArray? get() = values[ENUM]
  override val defaultValue: JsrValue? get() = values[DEFAULT]
  override val format: String? get() = values[FORMAT]
  override val minLength: Int? get() = values[MIN_LENGTH]?.toInt()
  override val maxLength: Int? get() = values[MAX_LENGTH]?.toInt()
  override val pattern: String? get() = values[PATTERN]
  override val minimum: Number? get() = this[MINIMUM]?.limit
  override val maximum: Number? get() = this[MAXIMUM]?.limit
  override val multipleOf: Number? get() = values[MULTIPLE_OF]
  override val minItems: Int? get() = values[MIN_ITEMS]?.toInt()
  override val maxItems: Int? get() = values[MAX_ITEMS]?.toInt()
  override val properties: Map<String, DraftSchema> get() = values[PROPERTIES].toDraftVersion()
  override val patternProperties: Map<String, DraftSchema>
    get() = values[PATTERN_PROPERTIES].toDraftVersion()
  override val additionalPropertiesSchema: D? get() = values[ADDITIONAL_PROPERTIES]?.toDraftVersion()
  override val requiresUniqueItems: Boolean get() = values[UNIQUE_ITEMS] ?: false

  override val additionalItemsSchema: D? get() = this[ITEMS]?.additionalItemSchema?.toDraftVersion()

  override val propertyDependencies: SetMultimap<String, String>
    get() = this[DEPENDENCIES]?.propertyDependencies ?: Multimaps.emptySetMultimap()

  override val propertySchemaDependencies: Map<String, DraftSchema>
    get() = this[DEPENDENCIES]?.dependencySchemas?.value.toDraftVersion()

  override val allItemSchema: D? get() = this[ITEMS]?.allItemSchema?.toDraftVersion()

  override val itemSchemas: List<DraftSchema> get() = this[ITEMS]?.indexedSchemas.toDraftVersion()

  // #####################################################
  // #####  KEYWORDS for Draft7    #######################
  // #####################################################

  override val ifSchema: DraftSchema? get() = this[IF]?.value?.toDraftVersion()
  override val elseSchema: DraftSchema? get() = this[ELSE]?.value?.toDraftVersion()
  override val thenSchema: DraftSchema? get() = this[THEN]?.value?.toDraftVersion()
  override val comment: String? by keywords(Keywords.COMMENT)
  override val isReadOnly: Boolean by keywords(Keywords.READ_ONLY, false)
  override val isWriteOnly: Boolean by keywords(Keywords.WRITE_ONLY, false)
  override val contentEncoding: String? by keywords(Keywords.CONTENT_ENCODING)
  override val contentMediaType: String? by keywords(Keywords.CONTENT_MEDIA_TYPE)

  // #####################################################
  // #####  KEYWORDS ONLY USED BY Draft3 -> Draft 4   ####
  // #####################################################

  override val isAnyType: Boolean get() = types.isEmpty()

  override val disallow: Set<JsonSchemaType>
    get() = keyword(Draft3Keywords.DISALLOW)?.disallowedTypes ?: emptySet()

  override val extendsSchema: DraftSchema? get() = values[Draft3Keywords.EXTENDS]?.toDraftVersion()
  override val isRequired: Boolean get() = values[Draft3Keywords.REQUIRED_DRAFT3] ?: false
  override val divisibleBy: Number? get() = multipleOf

  // #####################################################
  // #####  KEYWORDS SHARED BY Draft3 -> Draft 4      ####
  // #####################################################

  override val isExclusiveMinimum: Boolean
    get() = this[MINIMUM]?.isExclusive ?: false

  override val isExclusiveMaximum: Boolean
    get() = this[MAXIMUM]?.isExclusive ?: false

  // ######################################################
  // ###### Base Schema Methods Implemented  ##############
  // ######################################################

  private fun Schema.toDraftVersion(): D {
    return JsonSchema.draftSchema(this, type)
  }

  private fun Map<String, Schema>?.toDraftVersion(): Map<String, D> {
    return this?.mapValues { it.value.toDraftVersion() }?.freezeMap() ?: emptyMap()
  }

  private fun List<Schema>?.toDraftVersion(): List<D> {
    return this?.map { it.toDraftVersion() }?.freezeList() ?: emptyList()
  }

  override fun toJson(includeExtraProperties: Boolean): JsrObject {
    return jsrObject {
      when {
        schema is RefSchema-> REF.key *= schema.refURI
        REF in keywords-> REF.key *= keyword(REF)?.value?.toString()!!
        else-> {
          forEachSortedKeyword { keyword, keywordValue ->
            if (keyword.applicableVersions.contains(version)) {
              keywordValue.toJson(keyword, this, version, includeExtraProperties)
            } else {
              log.warn { "Keyword ${keyword.key} does not apply to version: [$version], only for ${keyword.applicableVersions}" }
            }
          }
          if (includeExtraProperties) {
            extraProperties.forEach { (prop, value) ->
              prop *= value
            }
          }
        }
      }
    }
  }

  fun forEachSortedKeyword(block: (KeywordInfo<*>, Keyword<*>) -> Unit) {
    keywords.map { it.key to it.value }
        .sortedBy { it.first.sortOrder }
        .forEach { (first, second) ->
          block(first, second)
        }
  }

  // ######################################################
  // ###### asDraft* for getting a different version ######
  // ######################################################

  //  override fun asDraft6(): Draft6Schema {
  //    return if (this is Draft6Schema) this
  //    else Draft6SchemaImpl(schemaLoader, this)
  //  }
  //
  //  override fun asDraft3(): Draft3Schema {
  //    return this as? Draft3Schema ?: Draft3SchemaImpl(schemaLoader, this)
  //  }
  //
  //  override fun asDraft7(): Draft7Schema {
  //    return this as? Draft7Schema ?: Draft7SchemaImpl(schemaLoader, this)
  //  }
  //
  //  override fun asDraft4(): Draft4Schema {
  //    return this as? Draft4Schema ?: Draft4SchemaImpl(schemaLoader, this)
  //  }

  override fun toString(): String = toString(true)

  override fun toString(includeExtraProperties: Boolean, indent: Boolean): String {
    return toJson(includeExtraProperties).writeJson(indent)
  }

  //  override fun toMutableSchema(): MutableSchema {
  //    return MutableJsonSchema(schemaLoader, fromSchema = this)
  //  }
  //
  //  override fun toMutableSchema(id: URI): MutableSchema {
  //    return MutableJsonSchema(schemaLoader, fromSchema = this, id = id)
  //  }

  // ##################################################################
  // ###### Helper methods for subclasses (accessing keywords) ########
  // ##################################################################

  override val examples: JsrArray get() = values[EXAMPLES] ?: jsrArrayOf()
  override val definitions: Map<String, DraftSchema> get() = values[DEFINITIONS].toDraftVersion()
  override val notSchema: DraftSchema? get() = values[Keywords.NOT]?.toDraftVersion()
  override val constValue: JsrValue? get() = values[Keywords.CONST]
  override val allOfSchemas: List<DraftSchema> get() = values[Keywords.ALL_OF].toDraftVersion()
  override val anyOfSchemas: List<DraftSchema> get() = values[Keywords.ANY_OF].toDraftVersion()
  override val oneOfSchemas: List<DraftSchema> get() = values[Keywords.ONE_OF].toDraftVersion()
  override val maxProperties: Int? get() = values[MAX_PROPERTIES]?.toInt()
  override val minProperties: Int? get() = values[MIN_PROPERTIES]?.toInt()
  override val requiredProperties: Set<String> get() = values[REQUIRED] ?: emptySet()
  override val exclusiveMinimum: Number? get() = this[MINIMUM]?.exclusiveLimit
  override val exclusiveMaximum: Number? get() = this[MAXIMUM]?.exclusiveLimit
  override val containsSchema: DraftSchema? get() = values[CONTAINS]?.toDraftVersion()
  override val propertyNameSchema: DraftSchema? get() = values[PROPERTY_NAMES]?.toDraftVersion()

  override val isAllowAdditionalItems: Boolean
    get() = this[ADDITIONAL_ITEMS]?.additionalItemSchema != nullSchema

  override val isAllowAdditionalProperties: Boolean
    get() = values[ADDITIONAL_PROPERTIES] != nullSchema

  override fun equals(other: Any?): Boolean {
    return when {
      this === other -> true
      other !is Schema && other !is DraftSchema -> false
      other is Schema-> other.keywords == keywords
      other is DraftSchema-> other.keywords == keywords
      else -> false
    }
  }

  override fun asDraft7(): Draft7Schema = this

  override fun asDraft6(): Draft6Schema = this

  override fun toDraft4(): Draft4Schema = this

  override fun toDraft3(): Draft3Schema = this

  override fun hashCode(): Int {
    return keywords.hashCode()
  }

  companion object {
    val log = mlogger {}

    /**
     * This function just ensures that the companion object is loaded and the init block is run.
     */
    fun initialize() {}

    //
    init {
      jsrJson.registerConversion<DraftSchema> {
        it.toJson(true)
      }
    }
  }
}

