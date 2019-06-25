package io.mverse.jsonschema.builder

import io.mverse.jsonschema.JsonSchemas
import io.mverse.jsonschema.MergeException
import io.mverse.jsonschema.MergeReport
import io.mverse.jsonschema.RefSchema
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaLocation
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.impl.JsonSchema
import io.mverse.jsonschema.impl.RefJsonSchema
import io.mverse.jsonschema.keyword
import io.mverse.jsonschema.keyword.BooleanKeyword
import io.mverse.jsonschema.keyword.DependenciesKeyword
import io.mverse.jsonschema.keyword.DollarSchemaKeyword
import io.mverse.jsonschema.keyword.DollarSchemaKeyword.Companion.emptyUri
import io.mverse.jsonschema.keyword.IdKeyword
import io.mverse.jsonschema.keyword.ItemsKeyword
import io.mverse.jsonschema.keyword.JsonArrayKeyword
import io.mverse.jsonschema.keyword.JsonValueKeyword
import io.mverse.jsonschema.keyword.JsrIterable
import io.mverse.jsonschema.keyword.Keyword
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.Keywords.ADDITIONAL_ITEMS
import io.mverse.jsonschema.keyword.Keywords.ADDITIONAL_PROPERTIES
import io.mverse.jsonschema.keyword.Keywords.ALL_OF
import io.mverse.jsonschema.keyword.Keywords.ANY_OF
import io.mverse.jsonschema.keyword.Keywords.COMMENT
import io.mverse.jsonschema.keyword.Keywords.CONST
import io.mverse.jsonschema.keyword.Keywords.CONTAINS
import io.mverse.jsonschema.keyword.Keywords.CONTENT_ENCODING
import io.mverse.jsonschema.keyword.Keywords.CONTENT_MEDIA_TYPE
import io.mverse.jsonschema.keyword.Keywords.DEFAULT
import io.mverse.jsonschema.keyword.Keywords.DEFINITIONS
import io.mverse.jsonschema.keyword.Keywords.DEPENDENCIES
import io.mverse.jsonschema.keyword.Keywords.DESCRIPTION
import io.mverse.jsonschema.keyword.Keywords.DOLLAR_ID
import io.mverse.jsonschema.keyword.Keywords.ELSE
import io.mverse.jsonschema.keyword.Keywords.ENUM
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
import io.mverse.jsonschema.keyword.Keywords.NOT
import io.mverse.jsonschema.keyword.Keywords.ONE_OF
import io.mverse.jsonschema.keyword.Keywords.PATTERN
import io.mverse.jsonschema.keyword.Keywords.PATTERN_PROPERTIES
import io.mverse.jsonschema.keyword.Keywords.PROPERTY_NAMES
import io.mverse.jsonschema.keyword.Keywords.READ_ONLY
import io.mverse.jsonschema.keyword.Keywords.REF
import io.mverse.jsonschema.keyword.Keywords.REQUIRED
import io.mverse.jsonschema.keyword.Keywords.SCHEMA
import io.mverse.jsonschema.keyword.Keywords.THEN
import io.mverse.jsonschema.keyword.Keywords.TITLE
import io.mverse.jsonschema.keyword.Keywords.TYPE
import io.mverse.jsonschema.keyword.Keywords.UNIQUE_ITEMS
import io.mverse.jsonschema.keyword.Keywords.WRITE_ONLY
import io.mverse.jsonschema.keyword.LimitKeyword
import io.mverse.jsonschema.keyword.LimitKeyword.Companion.maximumKeyword
import io.mverse.jsonschema.keyword.LimitKeyword.Companion.minimumKeyword
import io.mverse.jsonschema.keyword.NumberKeyword
import io.mverse.jsonschema.keyword.SchemaListKeyword
import io.mverse.jsonschema.keyword.SingleSchemaKeyword
import io.mverse.jsonschema.keyword.StringKeyword
import io.mverse.jsonschema.keyword.StringSetKeyword
import io.mverse.jsonschema.keyword.TypeKeyword
import io.mverse.jsonschema.keyword.URIKeyword
import io.mverse.jsonschema.keyword.iterableOf
import io.mverse.jsonschema.loading.LoadingReport
import io.mverse.jsonschema.loading.SchemaLoader
import io.mverse.jsonschema.loading.SchemaLoadingException
import io.mverse.jsonschema.mergeAdd
import io.mverse.jsonschema.mergeError
import io.mverse.jsonschema.utils.SchemaPaths
import io.mverse.jsonschema.utils.Schemas.nullSchema
import io.mverse.jsonschema.utils.isGeneratedURI
import io.mverse.logging.mlogger
import lang.collection.Multimaps
import lang.collection.SetMultimap
import lang.collection.freezeMap
import lang.hashKode
import lang.json.JsonPath
import lang.json.JsrObject
import lang.json.JsrValue
import lang.json.toJsrValue
import lang.json.unboxAsAny
import lang.net.URI
import lang.suppress.Suppressions.UNCHECKED_CAST
import lang.uuid.randomUUID
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Deprecated("Use MutableJsonSchema", replaceWith = ReplaceWith("MutableJsonSchema"))
typealias JsonSchemaBuilder = MutableJsonSchema

/**
 * Implementation of [MutableSchema]
 *
 * @param schemaLoader The loader used for this schema
 * @param internalKeywords The mutable keywords
 * @param extraProperties Extra properties not included in the json-schema spec
 * @param location The location for this mutable instance
 * @param currentDocument The current document for this schema, null if there is none.
 * @param loadingReport A loading report for reporting any errors
 */
data class MutableJsonSchema(
    override val schemaLoader: SchemaLoader,
    internal val internalKeywords: MutableMap<KeywordInfo<*>, Keyword<*>> = mutableMapOf(),
    override var extraProperties: MutableMap<String, JsrValue> = mutableMapOf(),
    override val location: SchemaLocation = SchemaPaths.fromNonSchemaSource(randomUUID()),
    override var currentDocument: JsrObject? = null,
    override var loadingReport: LoadingReport = LoadingReport(),
    override var baseSchema: Schema? = null) : MutableSchema {

  constructor(schemaLoader: SchemaLoader, fromSchema: Schema, id: URI)
      : this(schemaLoader = schemaLoader, fromSchema = fromSchema, location = SchemaLocation.builderFromId(id).build()) {
    this[DOLLAR_ID] = IdKeyword(id)
  }

  constructor(schemaLoader: SchemaLoader, fromSchema: Schema, location: SchemaLocation = fromSchema.location)
      : this(schemaLoader = schemaLoader, location = location, internalKeywords = fromSchema.keywords.toMutableMap(), extraProperties = fromSchema.extraProperties.toMutableMap()) {
    if (fromSchema is RefSchema) {
      if (fromSchema.refSchemaOrNull != null) {
        this.refSchema = fromSchema.refSchemaOrNull!!
      }
      this.ref = fromSchema.refURI
    }
  }

  constructor(schemaLoader: SchemaLoader, id: URI) : this(schemaLoader = schemaLoader, location = SchemaPaths.fromIdNonAbsolute(id)) {
    this[DOLLAR_ID] = IdKeyword(id)
  }

  constructor(schemaLoader: SchemaLoader, location: SchemaLocation, id: URI) : this(schemaLoader = schemaLoader, location = location) {
    this[DOLLAR_ID] = IdKeyword(id)
  }

  constructor(schemaLoader: SchemaLoader, location: SchemaLocation) : this(schemaLoader = schemaLoader, internalKeywords = mutableMapOf(), location = location)

  override val keywords: Map<KeywordInfo<*>, Keyword<*>> get() = internalKeywords

  override fun <K : Keyword<*>> set(key: KeywordInfo<K>, value: K?) {
    when (value) {
      null -> internalKeywords.remove(key)
      else -> internalKeywords[key] = value
    }
  }

  override operator fun minusAssign(key: KeywordInfo<*>) {
    internalKeywords.remove(key)
  }

  override fun invoke(block: MutableSchema.() -> Unit): MutableSchema {
    return apply(block)
  }

  override fun unsafeSet(keyword: KeywordInfo<*>, value: Keyword<*>) {
    internalKeywords[keyword] = value
  }

  // #######  KEYWORDS  ########  //

  override val id: URI? get() = values[Keywords.DOLLAR_ID]
  override var ref: Any?
    get() = values[REF]
    set(ref) {
      set(REF, ref?.let { URIKeyword(URI("$ref")) })
    }

  override var metaSchema: URI?
    get() = values[SCHEMA]
    set(value) {
      this[SCHEMA] = value?.let { DollarSchemaKeyword(value) }
    }

  override var refSchema: Schema? = null
  override var refURI: URI? by uri(REF)
  override var title: String? by string(TITLE)
  override var defaultValue: JsrValue? by jsrValue(DEFAULT)
  override var description: String? by string(DESCRIPTION)

  override var type: JsonSchemaType?
    get() = values[TYPE]?.firstOrNull()
    set(value) {
      if (value == null) {
        this -= TYPE
        return
      }
      val k = this[Keywords.TYPE]
      if (k == null) {
        internalKeywords[TYPE] = TypeKeyword(value)
      } else {
        internalKeywords[TYPE] = k.withAdditionalType(value)
      }
    }

  override var additionalProperties: Boolean
    get() = values[ADDITIONAL_PROPERTIES] != nullSchema
    set(value) {
      when (value) {
        true -> this -= ADDITIONAL_PROPERTIES
        false -> this[ADDITIONAL_PROPERTIES] = SingleSchemaKeyword(nullSchema)
      }
    }

  override var additionalItems: Boolean
    get() = values[ADDITIONAL_ITEMS] != nullSchema
    set(value) {
      when (value) {
        true -> this -= ADDITIONAL_ITEMS
        false -> this[ADDITIONAL_ITEMS] = ItemsKeyword(additionalItemSchema = nullSchema)
      }
    }

  override var format: String? by string(FORMAT)
  override var pattern: String? by string(PATTERN)

  override var types: Set<JsonSchemaType>
    get() = keyword(TYPE)?.types ?: emptySet()
    set(value) {
      val k = this[TYPE]
      if (k == null) {
        internalKeywords[TYPE] = TypeKeyword(value)
      } else {
        internalKeywords[TYPE] = k.copy(types = k.types + value)
      }
    }

  override var regex: Regex?
    get() = this.values[PATTERN]?.let { Regex(it) }
    set(value) = set(PATTERN, value?.let { StringKeyword(value.pattern) })

  override var minLength: Int? by int(MIN_LENGTH)
  override var maxLength: Int? by int(MAX_LENGTH)
  override var comment: String? by string(COMMENT)
  override var readOnly: Boolean by boolean(READ_ONLY)
  override var writeOnly: Boolean by boolean(WRITE_ONLY)
  override var requiredProperties: Set<String> by stringSet(REQUIRED)
  override var schemaOfAdditionalProperties: MutableSchema? by singleSchema(ADDITIONAL_PROPERTIES)

  override var contentEncoding: String? by string(CONTENT_ENCODING)
  override var contentMediaType: String? by string(CONTENT_MEDIA_TYPE)

  override var isUseSchemaKeyword: Boolean
    get() = keywords.containsKey(SCHEMA)
    set(value) {
      when (value) {
        true -> if (!keywords.containsKey(SCHEMA)) internalKeywords[SCHEMA] = DollarSchemaKeyword(emptyUri)
        false -> internalKeywords.remove(SCHEMA)
      }
    }

  override fun propertyNameSchema(block: MutableSchema.() -> Unit) {
    propertyNameSchema = subSchemaBuilder(PROPERTY_NAMES).apply(block)
  }

  override fun itemSchema(block: MutableSchema.() -> Unit) {
    itemSchemas = itemSchemas + subSchemaBuilder(Keywords.ITEMS).apply(block)
  }

  override fun oneOfSchema(block: MutableSchema.() -> Unit) {
    oneOfSchemas = oneOfSchemas + subSchemaBuilder(Keywords.ONE_OF).apply(block)
  }

  override fun allOfSchema(block: MutableSchema.() -> Unit) {
    allOfSchemas = allOfSchemas + subSchemaBuilder(Keywords.ALL_OF).apply(block)
  }

  override fun anyOfSchema(block: MutableSchema.() -> Unit) {
    anyOfSchemas = anyOfSchemas + subSchemaBuilder(Keywords.ANY_OF).apply(block)
  }

  override fun allItemsSchema(block: MutableSchema.() -> Unit) {
    this.allItemSchema = subSchemaBuilder(Keywords.ITEMS).apply(block)
  }

  override fun containsSchema(block: MutableSchema.() -> Unit) {
    this.containsSchema = subSchemaBuilder(Keywords.CONTAINS).apply(block)
  }

  override fun ifSchema(block: MutableSchema.() -> Unit) {
    ifSchema = subSchemaBuilder(Keywords.IF).apply(block)
  }

  override fun thenSchema(block: MutableSchema.() -> Unit) {
    thenSchema = subSchemaBuilder(Keywords.THEN).apply(block)
  }

  override fun elseSchema(block: MutableSchema.() -> Unit) {
    elseSchema = subSchemaBuilder(Keywords.ELSE).apply(block)
  }

  override fun notSchema(block: MutableSchema.() -> Unit) {
    notSchema = subSchemaBuilder(Keywords.NOT).apply(block)
  }

  override fun schemaOfAdditionalProperties(block: MutableSchema.() -> Unit) {
    schemaOfAdditionalProperties = subSchemaBuilder(keyword = Keywords.ADDITIONAL_PROPERTIES).apply(block)
  }

  override fun schemaOfAdditionalItems(block: MutableSchema.() -> Unit) {
    schemaOfAdditionalItems = subSchemaBuilder(keyword = Keywords.ADDITIONAL_ITEMS).apply(block)
  }

  override var propertyDependencies: SetMultimap<String, String>
    get() = this[DEPENDENCIES]
        ?.propertyDependencies
        ?: Multimaps.emptySetMultimap()
    set(value) {
      val dependencies = this[DEPENDENCIES] ?: DependenciesKeyword()
      internalKeywords[DEPENDENCIES] = dependencies.copy(propertyDependencies = value)
    }

  override var properties = MutableProperties(this)
  override var patternProperties = MutableSchemaMap(PATTERN_PROPERTIES, this)
  override var schemaDependencies = MutableSchemaDependencies(this)
  override var definitions = MutableSchemaMap(DEFINITIONS, this)

  override fun contains(keyword: KeywordInfo<*>): Boolean {
    return keywords.containsKey(keyword)
  }

  override var propertyNameSchema: MutableSchema? by singleSchema(PROPERTY_NAMES)
  override var minProperties: Int? by int(MIN_PROPERTIES)
  override var maxProperties: Int? by int(MAX_PROPERTIES)
  override var multipleOf: Number? by number(MULTIPLE_OF)

  override var exclusiveMinimum: Number?
    get() = this[MINIMUM]?.exclusiveLimit
    set(value) {
      when (value) {
        null -> this -= MINIMUM
        else -> {
          val limit: LimitKeyword = this[MINIMUM] ?: minimumKeyword()
          internalKeywords[MINIMUM] = limit.copy(exclusiveLimit = value)
        }
      }
    }

  override var minimum: Number?
    get() = this[MINIMUM]?.limit
    set(value) {
      when (value) {
        null -> this -= MINIMUM
        else -> {
          val limit: LimitKeyword = this[MINIMUM] ?: minimumKeyword()
          internalKeywords[MINIMUM] = limit.copy(limit = value)
        }
      }
    }

  override var exclusiveMaximum: Number?
    get() = this[MAXIMUM]?.exclusiveLimit
    set(value) {
      when (value) {
        null -> this -= MAXIMUM
        else -> {
          val limit: LimitKeyword = this[MAXIMUM] ?: maximumKeyword()
          internalKeywords[MAXIMUM] = limit.copy(exclusiveLimit = value)
        }
      }
    }

  override var maximum: Number?
    get() = this[MAXIMUM]?.limit
    set(value) {
      when (value) {
        null -> this -= MAXIMUM
        else -> {
          val limit: LimitKeyword = this[MAXIMUM] ?: maximumKeyword()
          internalKeywords[MAXIMUM] = limit.copy(limit = value)
        }
      }
    }

  override var needsUniqueItems: Boolean by boolean(UNIQUE_ITEMS)
  override var maxItems: Int? by int(MAX_ITEMS)
  override var minItems: Int? by int(MIN_ITEMS)

  override var schemaOfAdditionalItems: MutableSchema?
    get() = this[ITEMS]?.additionalItemSchema?.toMutableSchema()
    set(value) {
      val existing = this[ITEMS] ?: ItemsKeyword()
      val additionalItemSchema = when (value) {
        null -> null
        else -> buildSubSchema(value, ADDITIONAL_ITEMS)
      }
      this[ITEMS] = existing.copy(additionalItemSchema = additionalItemSchema)
    }

  override var containsSchema: MutableSchema? by singleSchema(CONTAINS)

  override var itemSchemas: List<MutableSchema>
    get() = values[ITEMS]?.map { it.toMutableSchema() } ?: emptyList()
    set(value) {
      val items = this[ITEMS] ?: ItemsKeyword()
      internalKeywords[ITEMS] = items.copy(indexedSchemas = buildSubSchemas(value, ITEMS))
    }

  override var allItemSchema: MutableSchema?
    get() = keyword(ITEMS)?.allItemSchema?.toMutableSchema()
    set(value) {
      val items = this[ITEMS] ?: ItemsKeyword()
      internalKeywords[ITEMS] = when (value) {
        null -> items.copy(allItemSchema = null)
        else -> items.copy(allItemSchema = buildSubSchema(value, ITEMS))
      }
    }

  override var notSchema: MutableSchema? by singleSchema(NOT)

  override var enumValues: JsrIterable?
    get() = values[ENUM]
    set(value) {
      this[ENUM] = value?.let { JsonArrayKeyword(value) }
    }

  override var const: Any?
    get() = values[CONST]?.unboxAsAny()
    set(value) = set(CONST, value?.let { JsonValueKeyword(toJsrValue(value)) })

  override var constValue: JsrValue? by jsrValue(CONST)
  override var oneOfSchemas: List<MutableSchema> by schemaList(ONE_OF)
  override var anyOfSchemas: List<MutableSchema> by schemaList(ANY_OF)
  override var allOfSchemas: List<MutableSchema> by schemaList(ALL_OF)
  override var ifSchema: MutableSchema? by singleSchema(IF)
  override var thenSchema: MutableSchema? by singleSchema(THEN)
  override var elseSchema: MutableSchema? by singleSchema(ELSE)

  override fun build(itemsLocation: SchemaLocation?, report: LoadingReport): Schema {
    // Use the location provided during building as an override
    var loc: SchemaLocation = itemsLocation ?: this.location
    if (id != null) {
      loc = loc.withId(this.id!!)
    }

    val finalLocation = loc

    val refSchema = this.refSchema
    val ref = this.ref
    val built = when {
      refSchema != null -> RefJsonSchema(schemaLoader, refURI = this.refSchema!!.absoluteURI,
          location = finalLocation,
          refSchema = this.refSchema!!)
      ref != null && currentDocument != null -> RefJsonSchema(schemaLoader = schemaLoader,
          refURI = this.refURI!!,
          currentDocument = currentDocument!!,
          location = finalLocation,
          version = JsonSchemaVersion.Draft7,
          report = report)
      ref != null -> RefJsonSchema(
          schemaLoader = schemaLoader,
          internalLocation = finalLocation,
          refURI = this.refURI!!,
          internalParent = null,
          version = JsonSchemaVersion.Draft7,
          refResolver = {
            schemaLoader.loadRefSchema(it, it.refURI, null, LoadingReport())
          })
      else -> JsonSchema(schemaLoader, finalLocation, this.keywords.freezeMap(),
          this.extraProperties.freezeMap(), JsonSchemaVersion.Draft7)
          .also { built ->
            if (!built.absoluteURI.isGeneratedURI() && built.location.jsonPath == JsonPath.rootPath) {
              schemaLoader += built
            }
          }
    }

    return built
  }

  fun enumValues(block: () -> JsrIterable) {
    this.enumValues = iterableOf(block)
  }

  override fun build(): Schema = this.build(block = {})
  override fun build(block: MutableSchema.() -> Unit): Schema {
    this.block()
    val location: SchemaLocation =
        @Suppress("USELESS_ELVIS", "SENSELESS_COMPARISON")
        when {
          this.id == null -> this.location ?: SchemaPaths.fromBuilder(this)
          this.location != null -> this.location.withId(this.id!!)
          else -> SchemaPaths.fromId(this.id!!)
        }

    val built = build(location, loadingReport)
    if (loadingReport.hasErrors()) {
      throw SchemaLoadingException(location.jsonPointerFragment, loadingReport)
    }
    return when(val baseSchema = baseSchema) {
      null-> built
      else->JsonSchemas.schemaMerger.merge(JsonPath.rootPath, baseSchema, built, MergeReport(), mergedId = built.id)
    }
  }

  override fun merge(path: JsonPath, other: Schema, report: MergeReport) {

  }

  override fun withLocation(location: SchemaLocation): MutableSchema {
    return this.copy(location = location)
  }

  fun subSchemaBuilder(keyword: KeywordInfo<*>, vararg child: String): MutableSchema {
    val childLocation = this.location.child(keyword).child(*child)
    return MutableJsonSchema(location = childLocation, schemaLoader = schemaLoader)
  }

  // #######################################################
  // LAZY GETTERS
  // #######################################################

  override fun equals(other: Any?): Boolean {
    return when {
      this === other -> true
      other !is MutableJsonSchema -> false
      this.keywords == other.keywords -> true
      else -> false
    }
  }

  private fun string(keyword: KeywordInfo<StringKeyword>): ReadWriteProperty<MutableJsonSchema, String?> = object : ReadWriteProperty<MutableJsonSchema, String?> {
    override fun getValue(thisRef: MutableJsonSchema, property: KProperty<*>): String? = values[keyword]
    override fun setValue(thisRef: MutableJsonSchema, property: KProperty<*>, value: String?) {
      this@MutableJsonSchema[keyword] = value?.let { StringKeyword(value) }
    }
  }

  private fun singleSchema(keyword: KeywordInfo<SingleSchemaKeyword>): ReadWriteProperty<MutableJsonSchema, MutableSchema?> = object : ReadWriteProperty<MutableJsonSchema, MutableSchema?> {
    override fun getValue(thisRef: MutableJsonSchema, property: KProperty<*>): MutableSchema? = values[keyword]?.toMutableSchema()
    override fun setValue(thisRef: MutableJsonSchema, property: KProperty<*>, value: MutableSchema?) {
      this@MutableJsonSchema[keyword] = value?.let { SingleSchemaKeyword(buildSubSchema(value, keyword)) }
    }
  }

  private fun boolean(keyword: KeywordInfo<BooleanKeyword>): ReadWriteProperty<MutableJsonSchema, Boolean> = object : ReadWriteProperty<MutableJsonSchema, Boolean> {
    override fun getValue(thisRef: MutableJsonSchema, property: KProperty<*>): Boolean = values[keyword] == true
    override fun setValue(thisRef: MutableJsonSchema, property: KProperty<*>, value: Boolean) {
      this@MutableJsonSchema[keyword] = BooleanKeyword(value)
    }
  }

  private fun number(keyword: KeywordInfo<NumberKeyword>): ReadWriteProperty<MutableJsonSchema, Number?> = object : ReadWriteProperty<MutableJsonSchema, Number?> {
    override fun getValue(thisRef: MutableJsonSchema, property: KProperty<*>): Number? = values[keyword]
    override fun setValue(thisRef: MutableJsonSchema, property: KProperty<*>, value: Number?) {
      this@MutableJsonSchema[keyword] = value?.let { NumberKeyword(value) }
    }
  }

  private fun int(keyword: KeywordInfo<NumberKeyword>): ReadWriteProperty<MutableJsonSchema, Int?> = object : ReadWriteProperty<MutableJsonSchema, Int?> {
    override fun getValue(thisRef: MutableJsonSchema, property: KProperty<*>): Int? = values[keyword]?.toInt()
    override fun setValue(thisRef: MutableJsonSchema, property: KProperty<*>, value: Int?) {
      this@MutableJsonSchema[keyword] = value?.let { NumberKeyword(value) }
    }
  }

  private fun jsrValue(keyword: KeywordInfo<JsonValueKeyword>): ReadWriteProperty<MutableJsonSchema, JsrValue?> = object : ReadWriteProperty<MutableJsonSchema, JsrValue?> {
    override fun getValue(thisRef: MutableJsonSchema, property: KProperty<*>): JsrValue? = values[keyword]
    override fun setValue(thisRef: MutableJsonSchema, property: KProperty<*>, value: JsrValue?) {
      this@MutableJsonSchema[keyword] = value?.let { JsonValueKeyword(value) }
    }
  }

  private fun stringSet(keyword: KeywordInfo<StringSetKeyword>): ReadWriteProperty<MutableJsonSchema, Set<String>> = object : ReadWriteProperty<MutableJsonSchema, Set<String>> {
    override fun getValue(thisRef: MutableJsonSchema, property: KProperty<*>): Set<String> = values[keyword]
        ?: emptySet()

    override fun setValue(thisRef: MutableJsonSchema, property: KProperty<*>, value: Set<String>) {
      this@MutableJsonSchema[keyword] = StringSetKeyword(value)
    }
  }

  private fun schemaList(keyword: KeywordInfo<SchemaListKeyword>): ReadWriteProperty<MutableJsonSchema, List<MutableSchema>> = object : ReadWriteProperty<MutableJsonSchema, List<MutableSchema>> {
    override fun getValue(thisRef: MutableJsonSchema, property: KProperty<*>): List<MutableSchema> = values[keyword]?.map { it.toMutableSchema() }
        ?: emptyList()

    override fun setValue(thisRef: MutableJsonSchema, property: KProperty<*>, value: List<MutableSchema>) {
      this@MutableJsonSchema[keyword] = SchemaListKeyword(buildSubSchemas(value, keyword))
    }
  }

  private fun uri(keyword: KeywordInfo<URIKeyword>): ReadWriteProperty<MutableJsonSchema, URI?> = object : ReadWriteProperty<MutableJsonSchema, URI?> {
    override fun getValue(thisRef: MutableJsonSchema, property: KProperty<*>): URI? = values[keyword]
    override fun setValue(thisRef: MutableJsonSchema, property: KProperty<*>, value: URI?) {
      this@MutableJsonSchema[keyword] = value?.let { URIKeyword(value) }
    }
  }

  override fun hashCode(): Int = hashKode(keywords)

  companion object {
    val log = mlogger {}
  }
}
