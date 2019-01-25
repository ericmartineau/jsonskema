package io.mverse.jsonschema.builder

import io.mverse.jsonschema.MergeException
import io.mverse.jsonschema.MergeReport
import io.mverse.jsonschema.MutableKeywordContainer
import io.mverse.jsonschema.RefSchema
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaLocation
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.impl.Draft7SchemaImpl
import io.mverse.jsonschema.impl.RefSchemaImpl
import io.mverse.jsonschema.keyword.BooleanKeyword
import io.mverse.jsonschema.keyword.DependenciesKeyword
import io.mverse.jsonschema.keyword.DollarSchemaKeyword
import io.mverse.jsonschema.keyword.DollarSchemaKeyword.Companion.emptyUri
import io.mverse.jsonschema.keyword.IdKeyword
import io.mverse.jsonschema.keyword.ItemsKeyword
import io.mverse.jsonschema.keyword.JsonArrayKeyword
import io.mverse.jsonschema.keyword.JsonValueKeyword
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
import io.mverse.jsonschema.keyword.Keywords.PROPERTIES
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
import io.mverse.jsonschema.keyword.SchemaMapKeyword
import io.mverse.jsonschema.keyword.SingleSchemaKeyword
import io.mverse.jsonschema.keyword.StringKeyword
import io.mverse.jsonschema.keyword.StringSetKeyword
import io.mverse.jsonschema.keyword.TypeKeyword
import io.mverse.jsonschema.keyword.URIKeyword
import io.mverse.jsonschema.loading.LoadingReport
import io.mverse.jsonschema.loading.SchemaLoader
import io.mverse.jsonschema.loading.SchemaLoadingException
import io.mverse.jsonschema.mergeAdd
import io.mverse.jsonschema.mergeError
import io.mverse.jsonschema.utils.SchemaPaths
import io.mverse.jsonschema.utils.Schemas.nullSchema
import io.mverse.jsonschema.utils.Schemas.nullSchemaBuilder
import io.mverse.logging.mlogger
import lang.collection.Multimaps
import lang.collection.SetMultimap
import lang.exception.illegalState
import lang.hashKode
import lang.json.JsonPath
import lang.json.JsrArray
import lang.json.JsrObject
import lang.json.JsrValue
import lang.json.toJsrValue
import lang.json.unboxAsAny
import lang.net.URI
import lang.suppress.Suppressions.Companion.UNCHECKED_CAST
import lang.uuid.randomUUID

@Deprecated("Use MutableJsonSchema", replaceWith = ReplaceWith("MutableJsonSchema"))
typealias JsonSchemaBuilder = MutableJsonSchema

class MutableJsonSchema(
    override val schemaLoader: SchemaLoader,
    keywords: MutableMap<KeywordInfo<*>, Keyword<*>> = mutableMapOf(),
    override var extraProperties: MutableMap<String, JsrValue> = mutableMapOf(),
    private val location: SchemaLocation = SchemaPaths.fromNonSchemaSource(randomUUID()),
    override var currentDocument: JsrObject? = null,
    override var loadingReport: LoadingReport = LoadingReport())
  : SchemaBuilder, MutableKeywordContainer(keywords = keywords) {

  constructor(schemaLoader: SchemaLoader, fromSchema: Schema, id: URI) : this(schemaLoader = schemaLoader, fromSchema = fromSchema,
      location = SchemaLocation.builderFromId(id).build()) {
    keywords[DOLLAR_ID] = IdKeyword(id)
  }

  constructor(schemaLoader: SchemaLoader, fromSchema: Schema, location: SchemaLocation) : this(schemaLoader = schemaLoader, location = location,
      keywords = fromSchema.keywords.toMutableMap(),
      extraProperties = fromSchema.extraProperties.toMutableMap())

  constructor(schemaLoader: SchemaLoader, fromSchema: Schema) : this(schemaLoader = schemaLoader, fromSchema = fromSchema,
      location = fromSchema.location)

  constructor(schemaLoader: SchemaLoader, fromSchema: RefSchema) : this(schemaLoader = schemaLoader, location = fromSchema.location) {
    if (fromSchema.refSchemaOrNull != null) {
      this.refSchema = fromSchema.refSchemaOrNull!!
    }
    this.ref = fromSchema.refURI
  }

  constructor(schemaLoader: SchemaLoader, id: URI) : this(schemaLoader = schemaLoader, location = SchemaPaths.fromIdNonAbsolute(id)) {
    keywords[DOLLAR_ID] = IdKeyword(id)
  }

  constructor(schemaLoader: SchemaLoader, location: SchemaLocation, id: URI) : this(schemaLoader = schemaLoader, location = location) {
    keywords[DOLLAR_ID] = IdKeyword(id)
  }

  constructor(schemaLoader: SchemaLoader, location: SchemaLocation) : this(schemaLoader = schemaLoader, keywords = mutableMapOf(), location = location)

  operator fun <X, K : Keyword<X>> set(keyword: KeywordInfo<K>, value: X?, block: (X) -> K) {
    when (value) {
      null -> keywords.remove(keyword)
      else -> keywords[keyword] = block(value)
    }
  }

  operator fun set(keyword: KeywordInfo<SingleSchemaKeyword>, value: SchemaBuilder?) {
    when (value) {
      null -> keywords.remove(keyword)
      else -> keywords[keyword] = SingleSchemaKeyword(buildSubSchema(value, keyword))
    }
  }

  operator fun set(keyword: KeywordInfo<SingleSchemaKeyword>, property: String, value: SchemaBuilder?) {
    when (value) {
      null -> keywords.remove(keyword)
      else -> keywords[keyword] = SingleSchemaKeyword(buildSubSchema(value, keyword, property))
    }
  }

  override fun <K : Keyword<*>> set(keyword: KeywordInfo<K>, value: K) {
    keywords[keyword] = value
  }

  operator fun <X, K : Keyword<X>> set(keyword: KeywordInfo<K>, value: X?) {
    @Suppress("unchecked_cast")
    when (value) {
      null -> keywords.remove(keyword)
      else -> keywords[keyword] = when (value) {
        is String -> StringKeyword(value)
        is Number -> NumberKeyword(value)
        is Boolean -> BooleanKeyword(value)
        is JsrArray -> JsonArrayKeyword(value)
        is JsrValue -> JsonValueKeyword(value)
        is Schema -> SingleSchemaKeyword(value)
        is List<*> -> SchemaListKeyword(value as List<Schema>)
        is Map<*, *> -> SchemaMapKeyword(value as Map<String, Schema>)
        is Set<*> -> StringSetKeyword(value as Set<String>)
        is URI -> URIKeyword(value)
        else -> illegalState("Dont know how to handle keyword $keyword, value: $value")
      }
    }
  }

  override fun invoke(block: SchemaBuilder.() -> Unit): SchemaBuilder {
    return apply(block)
  }

  // #######  KEYWORDS  ########  //

  override val id: URI? get() = values[Keywords.DOLLAR_ID]
  override var ref: Any?
    get() = values[REF]
    set(ref) {
      val uri = when (ref) {
        null -> null
        else -> URI(ref.toString())
      }
      set(REF, uri)
    }

  override var metaSchema: URI?
    get() {
      return values[SCHEMA]
    }
    set(value) {
      when (value) {
        null -> keywords.remove(SCHEMA)
        else -> set(SCHEMA, value)
      }
    }

  override fun merge(path: JsonPath, other: Schema, report: MergeReport) {
    other.extraProperties.forEach { (k, v) ->
      extraProperties[k] = v
    }

    other.keywords.forEach { (keyword, value) ->
      val kwPath = path.child(keyword.key)
      if (keyword !in this) {
        keywords[keyword] = value
        report += mergeAdd(kwPath, keyword)
      } else {
        @Suppress(UNCHECKED_CAST)
        val thisValue = keywords[keyword] as Keyword<Any>
        @Suppress(UNCHECKED_CAST)
        val otherValue = value as Keyword<Any>
        try {
          val mergeKeyword = thisValue.merge(kwPath, keyword, otherValue, report)
          keywords[keyword] = mergeKeyword
        } catch (e: MergeException) {
          report += mergeError(kwPath, keyword, e)
        }
      }
    }
  }

  override var refSchema: Schema? = null

  override var refURI: URI?
    get() = values[Keywords.REF]
    set(ref) = set(REF, ref)

  override var title: String?
    get() = values[TITLE]
    set(value) = set(TITLE, value)

  override var defaultValue: JsrValue?
    get() = values[DEFAULT]
    set(value) = set(DEFAULT, value)

  override var description: String?
    get() = values[DESCRIPTION]
    set(value) = set(DESCRIPTION, value)

  override var type: JsonSchemaType?
    get() = values[TYPE]?.firstOrNull()
    set(value) {
      if (value == null) {
        this -= TYPE
        return
      }
      val k = this[Keywords.TYPE]
      if (k == null) {
        keywords[TYPE] = TypeKeyword(value)
      } else {
        keywords[TYPE] = k.withAdditionalType(value)
      }
    }

  override var additionalProperties: Boolean
    get() = values[ADDITIONAL_PROPERTIES] != nullSchema
    set(value) {
      when (value) {
        true -> this -= ADDITIONAL_PROPERTIES
        false -> this[ADDITIONAL_PROPERTIES] = nullSchemaBuilder()
      }
    }

  override var format: String?
    get() = values[FORMAT]
    set(value) = set(FORMAT, value)

  override var pattern: String?
    get() = values[PATTERN]
    set(value) = set(PATTERN, value)

  override var types: Set<JsonSchemaType>
    get() = keyword(TYPE)?.types ?: emptySet()
    set(value) {
      val k = this[TYPE]
      if (k == null) {
        keywords[TYPE] = TypeKeyword(value)
      } else {
        keywords[TYPE] = k.copy(types = k.types + value)
      }
    }

  override var regex: Regex?
    get() = this.values[Keywords.PATTERN]?.let { Regex(it) }
    set(value) = set(PATTERN, value?.pattern)

  override var minLength: Int?
    get() = values[MIN_LENGTH]?.toInt()
    set(value) = set(MIN_LENGTH, value)

  override var maxLength: Int?
    get() = values[MAX_LENGTH]?.toInt()
    set(value) = set(MAX_LENGTH, value)

  override var comment: String?
    get() = values[COMMENT]
    set(value) = set(COMMENT, value)

  override var readOnly: Boolean
    get() = values[READ_ONLY] ?: false
    set(value) = set(READ_ONLY, value)

  override var writeOnly: Boolean
    get() = values[WRITE_ONLY] ?: false
    set(value) = set(WRITE_ONLY, value)

  override var requiredProperties: Set<String>
    get() = (values[REQUIRED] ?: emptySet()).toMutableSet()
    set(value) = set(REQUIRED, value)

  override var contentEncoding: String?
    get() = values[CONTENT_ENCODING]
    set(value) = set(CONTENT_ENCODING, value)

  override var contentMediaType: String?
    get() = values[CONTENT_MEDIA_TYPE]
    set(value) = set(CONTENT_MEDIA_TYPE, value)

  override var isUseSchemaKeyword: Boolean
    get() = keywords.containsKey(SCHEMA)
    set(value) {
      when (value) {
        true -> if (!keywords.containsKey(SCHEMA)) keywords[SCHEMA] = DollarSchemaKeyword(emptyUri)
        false -> keywords.remove(SCHEMA)
      }
    }

  override var schemaOfAdditionalProperties: MutableSchema?
    get() = values[ADDITIONAL_PROPERTIES]?.toMutableSchema()
    set(value) = set(ADDITIONAL_PROPERTIES, value)

  override var schemaDependencies: Map<String, SchemaBuilder>
    get() = values[DEPENDENCIES].toBuilders()
    set(value) {
      val existing = this[DEPENDENCIES] ?: DependenciesKeyword()
      this[DEPENDENCIES] = existing.copy(dependencySchemas = existing.dependencySchemas.copy(value.buildSchemaMap()))
    }

  fun Map<String, MutableSchema>.buildSchemaMap(): Map<String, Schema> = this.mapValues { e ->
    buildSubSchema(e.value, DEPENDENCIES, e.key)
  }

  fun Map<String, Schema>?.toBuilders(): Map<String, MutableSchema> = this?.mapValues { e ->
    e.value.toMutableSchema()
  } ?: emptyMap()

  override var propertyDependencies: SetMultimap<String, String>
    get() = this[DEPENDENCIES]
        ?.propertyDependencies
        ?: Multimaps.emptySetMultimap()
    set(value) {
      val dependencies = this[DEPENDENCIES] ?: DependenciesKeyword()
      keywords[DEPENDENCIES] = dependencies.copy(propertyDependencies = value)
    }

  override var properties: MutableSchemaMap = MutableSchemaMap(PROPERTIES, this)
  override var patternProperties = MutableSchemaMap(PATTERN_PROPERTIES, this)
  override var definitions = MutableSchemaMap(DEFINITIONS, this)

  override fun contains(keyword: KeywordInfo<*>): Boolean {
    return keywords.containsKey(keyword)
  }

  override var propertyNameSchema: MutableSchema?
    get() = values[PROPERTY_NAMES]?.toMutableSchema()
    set(value) = set(PROPERTY_NAMES, value)

  override var minProperties: Int?
    get() = values[MIN_PROPERTIES]?.toInt()
    set(value) = set(MIN_PROPERTIES, value)

  override var maxProperties: Int?
    get() = values[MAX_PROPERTIES]?.toInt()
    set(value) = set(MAX_PROPERTIES, value)

  override var multipleOf: Number?
    get() = values[MULTIPLE_OF]
    set(value) = set(MULTIPLE_OF, value)

  operator fun minusAssign(keyword: KeywordInfo<*>) {
    keywords.remove(keyword)
  }

  override var exclusiveMinimum: Number?
    get() = this[MINIMUM]?.exclusiveLimit
    set(value) {
      when (value) {
        null -> this -= MINIMUM
        else -> {
          val limit: LimitKeyword = this[MINIMUM] ?: minimumKeyword()
          keywords[MINIMUM] = limit.copy(exclusiveLimit = value)
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
          keywords[MINIMUM] = limit.copy(limit = value)
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
          keywords[MAXIMUM] = limit.copy(exclusiveLimit = value)
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
          keywords[MAXIMUM] = limit.copy(limit = value)
        }
      }
    }

  override var needsUniqueItems: Boolean
    get() = values[UNIQUE_ITEMS] ?: false
    set(value) = set(UNIQUE_ITEMS, value)

  override var maxItems: Int?
    get() = values[MAX_ITEMS]?.toInt()
    set(value) = set(MAX_ITEMS, value)

  override var minItems: Int?
    get() = values[MIN_ITEMS]?.toInt()
    set(value) = set(MIN_ITEMS, value)

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

  override var containsSchema: MutableSchema?
    get() = values[CONTAINS]?.toMutableSchema()
    set(value) = set(CONTAINS, value)

  override var itemSchemas: List<MutableSchema>
    get() = values[ITEMS]?.map { it.toMutableSchema() } ?: emptyList()
    set(value) {
      val items = this[ITEMS] ?: ItemsKeyword()
      keywords[ITEMS] = items.copy(indexedSchemas = buildSubSchemas(value, ITEMS))
    }

  override var allItemSchema: MutableSchema?
    get() = keyword(ITEMS)?.allItemSchema?.toMutableSchema()
    set(value) {
      val items = this[ITEMS] ?: ItemsKeyword()
      keywords[ITEMS] = when (value) {
        null -> items.copy(allItemSchema = null)
        else -> items.copy(allItemSchema = buildSubSchema(value, ITEMS))
      }
    }

  override var notSchema: MutableSchema?
    get() = values[NOT]?.toMutableSchema()
    set(value) = set(NOT, value)

  override var enumValues: JsrArray?
    get() = values[ENUM]
    set(value) = set(ENUM, value)

  override var const: Any?
    get() = values[CONST]?.unboxAsAny()
    set(value) = set(CONST, toJsrValue(value))

  override var constValue: JsrValue?
    get() = values[CONST]
    set(value) = set(CONST, value)

  override var oneOfSchemas: List<MutableSchema>
    get() = values[ONE_OF]?.map { it.toMutableSchema() } ?: emptyList()
    set(value) = set(ONE_OF, buildSubSchemas(value, ONE_OF))

  override var anyOfSchemas: List<MutableSchema>
    get() = values[ANY_OF]?.map { it.toMutableSchema() } ?: emptyList()
    set(value) = set(ANY_OF, buildSubSchemas(value, ANY_OF))

  override var allOfSchemas: List<MutableSchema>
    get() = values[ALL_OF]?.map { it.toMutableSchema() } ?: emptyList()
    set(value) = set(ALL_OF, buildSubSchemas(value, ALL_OF))

  override var ifSchema: MutableSchema?
    get() = values[IF]?.toMutableSchema()
    set(value) = set(IF, value)

  override var thenSchema: MutableSchema?
    get() = values[THEN]?.toMutableSchema()
    set(value) = set(THEN, value)

  override var elseSchema: MutableSchema?
    get() = values[ELSE]?.toMutableSchema()
    set(value) = set(ELSE, value)

  override fun build(itemsLocation: SchemaLocation?, report: LoadingReport): Schema {
    // Use the location provided during building as an override
    var loc: SchemaLocation = itemsLocation ?: this.location
    if (id != null) {
      loc = loc.withId(this.id!!)
    }

    val finalLocation = loc

    val thisSchemaURI = finalLocation.uniqueURI

    val cachedSchema = schemaLoader.findLoadedSchema(thisSchemaURI)
    if (cachedSchema != null) {
      return cachedSchema
    }

    val refSchema = this.refSchema
    val ref = this.ref
    return when {
      refSchema != null -> RefSchemaImpl(schemaLoader, refURI = this.refSchema!!.absoluteURI,
          location = finalLocation,
          refSchema = this.refSchema!!)
      ref != null -> RefSchemaImpl(refURI = this.refURI!!,
          factory = schemaLoader,
          currentDocument = currentDocument,
          location = finalLocation,
          report = report)
      else -> Draft7SchemaImpl(schemaLoader, finalLocation, this.keywords, this.extraProperties)
          .apply { schemaLoader += this }
    }
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
    return built
  }

  // #######################################################
  // HELPER FUNCTIONS
  // #######################################################

  private fun buildSubSchema(toBuild: MutableSchema, keyword: KeywordInfo<*>): Schema {
    val childLocation = this.location.child(keyword)
    return toBuild.build(childLocation, loadingReport)
  }

  override fun buildSubSchema(toBuild: MutableSchema, keyword: KeywordInfo<*>, path: String, vararg paths: String): Schema {
    val childLocation = this.location.child(keyword).child(path).child(*paths)
    return toBuild.build(childLocation, loadingReport)
  }

  private fun buildSubSchemas(toBuild: Collection<MutableSchema>, keyword: KeywordInfo<*>): List<Schema> {
    var idx = 0
    val childPath = this.location.child(keyword)
    return toBuild.map { builder -> builder.build(childPath.child(idx++), loadingReport) }
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

  override fun hashCode(): Int = hashKode(keywords)

  companion object {
    val log = mlogger {}
  }
}
