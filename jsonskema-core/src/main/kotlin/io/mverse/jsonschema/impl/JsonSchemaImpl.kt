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
import io.mverse.jsonschema.keyword.Keywords.Companion.ADDITIONAL_ITEMS
import io.mverse.jsonschema.keyword.Keywords.Companion.ADDITIONAL_PROPERTIES
import io.mverse.jsonschema.keyword.Keywords.Companion.CONTAINS
import io.mverse.jsonschema.keyword.Keywords.Companion.DEPENDENCIES
import io.mverse.jsonschema.keyword.Keywords.Companion.ITEMS
import io.mverse.jsonschema.keyword.Keywords.Companion.MAXIMUM
import io.mverse.jsonschema.keyword.Keywords.Companion.MAX_ITEMS
import io.mverse.jsonschema.keyword.Keywords.Companion.MAX_LENGTH
import io.mverse.jsonschema.keyword.Keywords.Companion.MAX_PROPERTIES
import io.mverse.jsonschema.keyword.Keywords.Companion.MINIMUM
import io.mverse.jsonschema.keyword.Keywords.Companion.MIN_ITEMS
import io.mverse.jsonschema.keyword.Keywords.Companion.MIN_LENGTH
import io.mverse.jsonschema.keyword.Keywords.Companion.MIN_PROPERTIES
import io.mverse.jsonschema.keyword.Keywords.Companion.MULTIPLE_OF
import io.mverse.jsonschema.keyword.Keywords.Companion.PATTERN
import io.mverse.jsonschema.keyword.Keywords.Companion.PATTERN_PROPERTIES
import io.mverse.jsonschema.keyword.Keywords.Companion.PROPERTIES
import io.mverse.jsonschema.keyword.Keywords.Companion.PROPERTY_NAMES
import io.mverse.jsonschema.keyword.Keywords.Companion.REQUIRED
import io.mverse.jsonschema.keyword.Keywords.Companion.UNIQUE_ITEMS
import io.mverse.jsonschema.utils.Schemas.nullSchema
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.json
import lang.Logger
import lang.SetMultimap
import lang.URI
import lang.hashKode

val log: Logger = Logger("JsonSchemaImpl")

abstract class JsonSchemaImpl<D : DraftSchema<D>>
(
    override val location: SchemaLocation,
    override val keywords: Map<KeywordInfo<*>, JsonSchemaKeyword<*>> = emptyMap(),
    override val extraProperties: Map<String, JsonElement> = emptyMap(),
    override val version: JsonSchemaVersion = JsonSchemaVersion.latest())

  : DraftSchema<D>, KeywordContainer(keywords) {

  // ######################################################
  // ###### Getters for common keywords (draft3-6) ########
  // ######################################################

  override val id: URI? by keywords(Keywords.DOLLAR_ID)
  override val schemaURI: URI? get() = version?.metaschemaURI
  override val title: String? by keywords(Keywords.TITLE)
  override val description: String? by keywords(Keywords.DESCRIPTION)
  override val types: Set<JsonSchemaType> by keywords(Keywords.TYPE, emptySet())
  override val enumValues: JsonArray? by keywords(Keywords.ENUM)
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
    keyword(DEPENDENCIES)?.propertyDependencies ?: SetMultimap()
  }

  override val propertySchemaDependencies: Map<String, Schema> by lazy {
    keyword(DEPENDENCIES)?.dependencySchemas?.schemas ?: emptyMap()
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
      val builder = this
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
    return if (this is Draft6Schema) {
      this as Draft6Schema
    } else Draft6SchemaImpl(this)
  }

  override fun asDraft3(): Draft3Schema {
    return if (this is Draft3Schema) {
      this as Draft3Schema
    } else Draft3SchemaImpl(this)
  }

  override fun asDraft7(): Draft7Schema {
    return this as? Draft7Schema ?: Draft7SchemaImpl(this)
  }

  override fun asDraft4(): Draft4Schema {
    return if (this is Draft4Schema) {
      this as Draft4Schema
    } else Draft4SchemaImpl(this)
  }

  override fun toString(): String {
    return toJson(version).toString()
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

  protected open val examples: JsonArray by keywords(Keywords.EXAMPLES, JsonArray(emptyList()))
  protected open val definitions: Map<String, Schema> by keywords(Keywords.DEFINITIONS, emptyMap())
  protected open val notSchema: Schema? by keywords(Keywords.NOT)
  protected open val constValue: JsonElement? by keywords(Keywords.CONST)
  protected open val allOfSchemas: List<Schema> by keywords(Keywords.ALL_OF, emptyList())
  protected open val anyOfSchemas: List<Schema> by keywords(Keywords.ANY_OF, emptyList())
  protected open val oneOfSchemas: List<Schema> by keywords(Keywords.ONE_OF, emptyList())
  protected open val maxProperties: Int? by numberKeyword(MAX_PROPERTIES)
  protected open val minProperties: Int? by numberKeyword(MIN_PROPERTIES)
  protected open val requiredProperties: Set<String> by keywords(REQUIRED, emptySet())
  protected open val exclusiveMinimum: Number? by lazy { keyword(MINIMUM)?.exclusive }
  protected open val exclusiveMaximum: Number? by lazy { keyword(MAXIMUM)?.exclusive }
  protected open val containsSchema: Schema? by keywords(CONTAINS)
  protected open val propertyNameSchema: Schema? by keywords(PROPERTY_NAMES)

  protected open val isAllowAdditionalItems: Boolean by lazy {
    keyword(ADDITIONAL_ITEMS)?.additionalItemSchema != nullSchema()
  }

  protected open val isAllowAdditionalProperties: Boolean by lazy {
    keyword(ADDITIONAL_PROPERTIES)?.schema != nullSchema()
  }
  //  @SuppressWarnings("unchecked")
  //  @Nullable
  //  protected fun uri(keywordType: KeywordInfo<out URIKeyword>): URI {
  //    return keywordValue<Object>(keywordType).orElse(null)
  //  }

  //  @SuppressWarnings("unchecked")
  //  @Nullable
  //  protected fun string(stringKeyword: KeywordInfo<StringKeyword>): String {
  //    checkNotNull(stringKeyword, "stringKeyword must not be null")
  //    return keywordValue<Object>(stringKeyword).orElse(null)
  //  }
  //

  //
  //  protected fun schemaList(keyword: KeywordInfo<out SchemaListKeyword>): List<Schema> {
  //    checkNotNull(keyword, "keyword must not be null")
  //    return keyword(keyword).map(???({ SchemaListKeyword.getSubschemas() })).orElse(Collections.emptyList())
  //  }
  //
  //  protected fun schemaMap(keyword: KeywordInfo<SchemaMapKeyword>): Map<String, Schema> {
  //    checkNotNull(keyword, "keyword must not be null")
  //    return keyword(keyword).map(???({ SchemaMapKeyword.getSchemas() })).orElse(Collections.emptyMap())
  //  }
  //
  //  @SuppressWarnings("unchecked")
  //  protected fun <X> keywordValue(keyword: KeywordInfo<out JsonSchemaKeywordImpl<X>>): Optional<X> {
  //    checkNotNull(keyword, "keyword must not be null")
  //    val keywordValue = keywords[keyword] as JsonSchemaKeywordImpl<X>
  //    return if (keywordValue == null) Optional.empty() else Optional.ofNullable(keywordValue!!.getKeywordValue())
  //  }

  //  protected fun examples(): JsonArray {
  //    return keyword<JsonSchemaKeyword>(Keywords.EXAMPLES)
  //        .map(???({ JsonArrayKeyword.getKeywordValue() }))
  //    .orElse(JsonUtils.emptyJsonArray())
  //  }
  //
  //  protected fun definitions(): Map<String, Schema> {
  //    return schemaMap(Keywords.DEFINITIONS)
  //  }
  //
  //  protected fun types(): Set<JsonSchemaType> {
  //    return keyword<JsonSchemaKeyword>(Keywords.TYPE)
  //        .map(???({ TypeKeyword.getTypes() }))
  //    .orElse(Collections.emptySet())
  //  }
  //
  //  protected fun enumValues(): Optional<JsonArray> {
  //    return keywordValue<Object>(Keywords.ENUM)
  //  }
  //
  //  protected fun defaultValue(): Optional<JsonValue> {
  //    return keywordValue<Object>(Keywords.DEFAULT)
  //  }
  //
  //  protected fun notSchema(): Optional<Schema> {
  //    return keywordValue<Object>(Keywords.NOT)
  //  }
  //
  //  protected fun constValue(): Optional<JsonValue> {
  //    return keywordValue<Object>(Keywords.CONST)
  //  }
  //
  //  protected fun allOfSchemas(): List<Schema> {
  //    return schemaList(Keywords.ALL_OF)
  //  }
  //
  //  protected fun anyOfSchemas(): List<Schema> {
  //    return schemaList(Keywords.ANY_OF)
  //  }
  //
  //  protected fun oneOfSchemas(): List<Schema> {
  //    return schemaList(Keywords.ONE_OF)
  //  }
  //
  //  protected fun format(): String {
  //    return string(Keywords.FORMAT)
  //  }
  //
  //  protected fun minLength(): Integer {
  //    return keywordValue<Object>(Keywords.MIN_LENGTH)
  //        .map(???({ Number.intValue() }))
  //    .orElse(null)
  //  }
  //
  //  protected fun maxLength(): Integer {
  //    return keywordValue<Object>(Keywords.MAX_LENGTH)
  //        .map(???({ Number.intValue() }))
  //    .orElse(null)
  //  }
  //
  //  protected fun pattern(): String {
  //    return string(Keywords.PATTERN)
  //  }
  //
  //  protected fun multipleOf(): Number {
  //    return keywordValue<Object>(Keywords.MULTIPLE_OF).orElse(null)
  //  }
  //
  //  protected fun maximum(): Number {
  //    return keyword<JsonSchemaKeyword>(Keywords.MAXIMUM).map(???({ LimitKeyword.getLimit() })).orElse(null)
  //  }
  //
  //  protected fun minimum(): Number {
  //    return keyword<JsonSchemaKeyword>(Keywords.MINIMUM).map(???({ LimitKeyword.getLimit() })).orElse(null)
  //  }
  //
  //  protected fun exclusiveMinimum(): Number {
  //    return keyword<JsonSchemaKeyword>(Keywords.MINIMUM).map(???({ LimitKeyword.getExclusiveLimit() })).orElse(null)
  //  }
  //
  //  protected fun exclusiveMaximum(): Number {
  //    return keyword<JsonSchemaKeyword>(Keywords.MAXIMUM).map(???({ LimitKeyword.getExclusiveLimit() })).orElse(null)
  //  }
  //
  //  protected fun minItems(): Integer {
  //    return getInteger(Keywords.MIN_ITEMS)
  //  }
  //
  //  protected fun getInteger(keyword: KeywordInfo<NumberKeyword>): Integer {
  //    checkNotNull(keyword, "keyword must not be null")
  //    return keywordValue<Object>(keyword).map(???({ Number.intValue() })).orElse(null)
  //  }
  //
  //  protected fun maxItems(): Integer {
  //    return getInteger(Keywords.MAX_ITEMS)
  //  }
  //
  //  protected fun allItemSchema(): Optional<Schema> {
  //    return keyword<JsonSchemaKeyword>(Keywords.ITEMS)
  //        .flatMap(???({ ItemsKeyword.getAllItemSchema() }))
  //  }
  //
  //  protected fun itemSchemas(): List<Schema> {
  //    return keyword<JsonSchemaKeyword>(Keywords.ITEMS)
  //        .map(???({ ItemsKeyword.getIndexedSchemas() }))
  //    .orElse(Collections.emptyList())
  //  }
  //
  //  protected fun additionalItemsSchema(): Optional<Schema> {
  //    return keyword<JsonSchemaKeyword>(Keywords.ITEMS).flatMap(???({ ItemsKeyword.getAdditionalItemSchema() }))
  //  }
  //
  //  protected fun containsSchema(): Optional<Schema> {
  //    return keywordValue<Object>(Keywords.CONTAINS)
  //  }
  //
  //  protected fun uniqueItems(): Boolean {
  //    return keywordValue<Object>(Keywords.UNIQUE_ITEMS).orElse(false)
  //  }
  //
  //  protected fun properties(): Map<String, Schema> {
  //    return schemaMap(Keywords.PROPERTIES)
  //  }
  //
  //  protected fun patternProperties(): Map<String, Schema> {
  //    return schemaMap(Keywords.PATTERN_PROPERTIES)
  //  }
  //
  //  protected fun additionalPropertiesSchema(): Optional<Schema> {
  //    return keywordValue<Object>(Keywords.ADDITIONAL_PROPERTIES)
  //  }
  //
  //  protected fun propertyNameSchema(): Optional<Schema> {
  //    return keywordValue<Object>(Keywords.PROPERTY_NAMES)
  //  }
  //
  //  protected fun propertyDependencies(): SetMultimap<String, String> {
  //    return keyword<JsonSchemaKeyword>(Keywords.DEPENDENCIES)
  //        .map(???({ DependenciesKeyword.getPropertyDependencies() }))
  //    .orElse(ImmutableSetMultimap.of())
  //  }
  //
  //  protected fun propertySchemaDependencies(): Map<String, Schema> {
  //    return keyword<JsonSchemaKeyword>(Keywords.DEPENDENCIES)
  //        .map(???({ DependenciesKeyword.getDependencySchemas() }))
  //    .map(???({ SchemaMapKeyword.getSchemas() }))
  //    .orElse(ImmutableMap.of())
  //  }
  //
  //  protected fun maxProperties(): Integer {
  //    return getInteger(Keywords.MAX_PROPERTIES)
  //  }
  //
  //  protected fun minProperties(): Integer {
  //    return getInteger(Keywords.MIN_PROPERTIES)
  //  }
  //
  //  protected fun requiredProperties(): Set<String> {
  //    return keyword<JsonSchemaKeyword>(Keywords.REQUIRED)
  //        .map(???({ StringSetKeyword.getKeywordValue() }))
  //    .orElse(Collections.emptySet())
  //  }

  override fun equals(other: Any?): Boolean {
    return when {
      this === other -> true
      other !is JsonSchemaImpl<*> -> false
      else -> other.keywords == this.keywords
    }
  }

  override fun hashCode(): Int {
    return hashKode(keywords)
  }
}
