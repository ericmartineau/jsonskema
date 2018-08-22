package io.mverse.jsonschema.builder

import io.mverse.jsonschema.MutableKeywordContainer
import io.mverse.jsonschema.RefSchema
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaBuilder
import io.mverse.jsonschema.SchemaLocation
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.impl.Draft7SchemaImpl
import io.mverse.jsonschema.impl.RefSchemaImpl
import io.mverse.jsonschema.jsonschemaBuilder
import io.mverse.jsonschema.keyword.BooleanKeyword
import io.mverse.jsonschema.keyword.DependenciesKeyword
import io.mverse.jsonschema.keyword.DollarSchemaKeyword
import io.mverse.jsonschema.keyword.IdKeyword
import io.mverse.jsonschema.keyword.ItemsKeyword
import io.mverse.jsonschema.keyword.JsonArrayKeyword
import io.mverse.jsonschema.keyword.JsonSchemaKeyword
import io.mverse.jsonschema.keyword.JsonValueKeyword
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.Keywords.ADDITIONAL_ITEMS
import io.mverse.jsonschema.keyword.Keywords.ADDITIONAL_PROPERTIES
import io.mverse.jsonschema.keyword.Keywords.COMMENT
import io.mverse.jsonschema.keyword.Keywords.DESCRIPTION
import io.mverse.jsonschema.keyword.Keywords.DOLLAR_ID
import io.mverse.jsonschema.keyword.Keywords.ELSE
import io.mverse.jsonschema.keyword.Keywords.FORMAT
import io.mverse.jsonschema.keyword.Keywords.IF
import io.mverse.jsonschema.keyword.Keywords.ITEMS
import io.mverse.jsonschema.keyword.Keywords.PATTERN
import io.mverse.jsonschema.keyword.Keywords.PROPERTIES
import io.mverse.jsonschema.keyword.Keywords.READ_ONLY
import io.mverse.jsonschema.keyword.Keywords.REF
import io.mverse.jsonschema.keyword.Keywords.SCHEMA
import io.mverse.jsonschema.keyword.Keywords.THEN
import io.mverse.jsonschema.keyword.Keywords.TYPE
import io.mverse.jsonschema.keyword.Keywords.WRITE_ONLY
import io.mverse.jsonschema.keyword.LimitKeyword
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
import io.mverse.jsonschema.utils.SchemaPaths
import io.mverse.jsonschema.utils.Schemas.falseSchemaBuilder
import io.mverse.jsonschema.utils.Schemas.nullSchema
import io.mverse.jsonschema.utils.Schemas.nullSchemaBuilder
import kotlinx.serialization.json.JsonElement
import lang.Pattern
import lang.URI
import lang.hashKode
import lang.json.toJsonArray
import lang.json.toJsonLiteral
import lang.randomUUID

open class JsonSchemaBuilder(
    keywords: MutableMap<KeywordInfo<*>, JsonSchemaKeyword<*>> = mutableMapOf(),
    private val extraProperties: MutableMap<String, JsonElement> = mutableMapOf(),
    private val location: SchemaLocation = SchemaPaths.fromNonSchemaSource(randomUUID()),
    private var currentDocument: kotlinx.serialization.json.JsonObject? = null,
    private var schemaFactory: SchemaLoader? = null,
    private var loadingReport: LoadingReport = LoadingReport())
  : SchemaBuilder<JsonSchemaBuilder>, MutableKeywordContainer(keywords = keywords) {

  constructor(fromSchema: Schema, id: URI) : this(fromSchema, SchemaLocation.builderFromId(id).build()) {
    this.setOrRemoveId(id)
  }

  constructor(fromSchema: Schema, location: SchemaLocation) : this(location = location,
      keywords = fromSchema.keywords.toMutableMap(),
      extraProperties = fromSchema.extraProperties.toMutableMap())

  constructor(fromSchema: Schema) : this(fromSchema, fromSchema.location)

  constructor(fromSchema: RefSchema) : this(location = fromSchema.location) {
    this.ref(fromSchema.refURI)
  }

  constructor(id: URI) : this(location = SchemaPaths.fromIdNonAbsolute(id)) {
    this.setOrRemoveId(id)
  }

  constructor(location: SchemaLocation, id: URI) : this(location = location) {
    this.setOrRemoveId(id)
  }

  constructor(location: SchemaLocation) : this(keywords = mutableMapOf(), location = location)

  override val id: URI? get() = getKeyword(Keywords.DOLLAR_ID)?.value
  override var ref: Any?
    get() = getKeyword(Keywords.REF)?.value
    set(ref) {
      addOrRemoveURI(REF, ref?.let { URI(it.toString()) })
    }

  override var refURI: URI?
    get() = getKeyword(Keywords.REF)?.value
    set(ref) { addOrRemoveURI(REF, ref) }

  override var title: String? by mutableKeyword(Keywords.TITLE)
  override var defaultValue: JsonElement? by mutableKeyword(Keywords.DEFAULT)

  override var description: String? by mutableKeyword(DESCRIPTION)
  override var type: JsonSchemaType?
    get() = getKeyword(TYPE)?.types?.firstOrNull()
    set(type) {
      this.removeIfNecessary(TYPE, type)
      if (type != null) {
        this.type(type)
      }
    }

  override var additionalProperties: Boolean
    get() {
      val keyword = getKeyword(ADDITIONAL_PROPERTIES)
      return keyword?.value != nullSchema
    }
    set(value) {
      when(value) {
        true-> keywords.remove(ADDITIONAL_PROPERTIES)
        false-> addOrRemoveSchema(ADDITIONAL_PROPERTIES, nullSchemaBuilder())
      }
    }

  override var format: String? by mutableKeyword(FORMAT)
  override var pattern: String? by mutableKeyword(PATTERN)

  override fun withSchema(): JsonSchemaBuilder = apply { keywords[SCHEMA] = DollarSchemaKeyword() }
  override fun withoutSchema(): JsonSchemaBuilder = apply { keywords -= SCHEMA }
  override fun ref(ref: URI): JsonSchemaBuilder = apply { this.ref = ref }
  override fun ref(ref: String): JsonSchemaBuilder = apply { this.ref = URI(ref) }

  override fun title(title: String): JsonSchemaBuilder {
    return addOrRemoveString(Keywords.TITLE, title)
  }

  override fun defaultValue(defaultValue: JsonElement): JsonSchemaBuilder {
    return addOrRemoveJsonElement(Keywords.DEFAULT, defaultValue)
  }

  override fun description(description: String): JsonSchemaBuilder {
    return addOrRemoveString(Keywords.DESCRIPTION, description)
  }

  override fun type(requiredType: JsonSchemaType): JsonSchemaBuilder {
    val existingValue = getKeyword(Keywords.TYPE)
    if (existingValue == null) {
      keywords.put(Keywords.TYPE, TypeKeyword(requiredType))
    } else {
      keywords.put(Keywords.TYPE, existingValue.withAdditionalType(requiredType))
    }
    return this
  }

  override fun orType(requiredType: JsonSchemaType): JsonSchemaBuilder {
    return type(requiredType)
  }

  override fun types(requiredTypes: Set<JsonSchemaType>?): JsonSchemaBuilder {
    if (requiredTypes != null) {
      keywords[Keywords.TYPE] = TypeKeyword(requiredTypes)
    } else {
      clearTypes()
    }
    return this
  }

  // #############################
  // BASIC SCHEMA METADATA SETTERS
  // #############################

  override fun comment(comment: String): JsonSchemaBuilder {
    return addOrRemoveString(COMMENT, comment)
  }

  override fun clearTypes(): JsonSchemaBuilder {
    keywords.remove(Keywords.TYPE)
    return this
  }

  override fun readOnly(value: Boolean): JsonSchemaBuilder {
    return addOrRemoveBoolean(READ_ONLY, value)
  }

  override fun writeOnly(value: Boolean): JsonSchemaBuilder {
    return addOrRemoveBoolean(WRITE_ONLY, value)
  }

  override fun pattern(pattern: String): JsonSchemaBuilder {
    return pattern(Pattern(pattern))
  }

  override fun pattern(pattern: Pattern): JsonSchemaBuilder {
    this.addOrRemoveString(Keywords.PATTERN, pattern.regex)
    return this
  }

  override fun minLength(minLength: Int): JsonSchemaBuilder {
    this.addOrRemoveNumber(Keywords.MIN_LENGTH, minLength)
    return this
  }

  override fun maxLength(maxLength: Int): JsonSchemaBuilder {
    this.addOrRemoveNumber(Keywords.MAX_LENGTH, maxLength)
    return this
  }

  override fun format(format: String): JsonSchemaBuilder {
    this.addOrRemoveString(Keywords.FORMAT, format)
    return this
  }

  // #################################
  // SHARED VALIDATION KEYWORD SETTERS
  // #################################

  override fun schemaOfAdditionalProperties(schemaOfAdditionalProperties: SchemaBuilder<*>): JsonSchemaBuilder {
    this.addOrRemoveSchema(Keywords.ADDITIONAL_PROPERTIES, schemaOfAdditionalProperties)
    return this
  }

  override fun schemaDependency(property: String, dependency: SchemaBuilder<*>): JsonSchemaBuilder {
    val built = buildSubSchema(dependency, Keywords.DEPENDENCIES, property)
    return updateKeyword(Keywords.DEPENDENCIES,
        { DependenciesKeyword() },
        { dependenciesKeyword ->
          dependenciesKeyword.toBuilder()
              .addDependencySchema(property, built)
              .build()
        })
  }

  override fun propertyDependency(ifPresent: String, thenRequireThisProperty: String): JsonSchemaBuilder {
    return updateKeyword(Keywords.DEPENDENCIES,
        { DependenciesKeyword() },
        { dependenciesKeyword ->
          dependenciesKeyword.toBuilder()
              .propertyDependency(ifPresent, thenRequireThisProperty)
              .build()
        })
  }

  override fun requiredProperty(requiredProperty: String): JsonSchemaBuilder {
    return updateKeyword(Keywords.REQUIRED,
        { StringSetKeyword() },
        { stringSetKeyword -> stringSetKeyword.withAnotherValue(requiredProperty) })
  }

  override fun propertySchema(propertySchemaKey: String, propertySchemaValue: SchemaBuilder<*>): JsonSchemaBuilder {
    this.putKeywordSchema(Keywords.PROPERTIES, propertySchemaKey, propertySchemaValue)
    return this
  }

  override fun propertySchema(propertySchemaKey: String, block: SchemaBuilder<*>.() -> Unit): JsonSchemaBuilder {
    this.putKeywordSchema(Keywords.PROPERTIES, propertySchemaKey, jsonschemaBuilder(init = block))
    return this
  }

  override fun updatePropertySchema(propertyName: String,
                                    updater: (SchemaBuilder<*>) -> SchemaBuilder<*>): JsonSchemaBuilder {
    this.updateKeyword(PROPERTIES, { SchemaMapKeyword() }) { schemaMap ->
      val schema = schemaMap.value[propertyName]
      val updateBuilder = schema?.toBuilder<JsonSchemaBuilder>() ?: JsonSchemaBuilder()

      val updatedSchema = updater(updateBuilder).build()
      return@updateKeyword schemaMap + (propertyName to updatedSchema)
    }
    return this
  }

  override fun propertyNameSchema(propertyNameSchema: SchemaBuilder<*>): JsonSchemaBuilder {
    this.addOrRemoveSchema(Keywords.PROPERTY_NAMES, propertyNameSchema)
    return this
  }

  override fun patternProperty(pattern: Pattern, schema: SchemaBuilder<*>): JsonSchemaBuilder {
    this.putKeywordSchema(Keywords.PATTERN_PROPERTIES, pattern.regex, schema)
    return this
  }

  override fun patternProperty(pattern: String, schema: SchemaBuilder<*>): JsonSchemaBuilder {
    this.putKeywordSchema(Keywords.PATTERN_PROPERTIES, pattern, schema)
    return this
  }

  override fun minProperties(minProperties: Int): JsonSchemaBuilder {
    this.addOrRemoveNumber(Keywords.MIN_PROPERTIES, minProperties)
    return this
  }

  override fun maxProperties(maxProperties: Int): JsonSchemaBuilder {
    this.addOrRemoveNumber(Keywords.MAX_PROPERTIES, maxProperties)
    return this
  }

  override fun clearRequiredProperties(): JsonSchemaBuilder {
    keywords.remove(Keywords.REQUIRED)
    return this
  }

  override fun clearPropertySchemas(): JsonSchemaBuilder {
    keywords.remove(Keywords.PROPERTIES)
    return this
  }

  override fun multipleOf(multipleOf: Number): JsonSchemaBuilder {
    this.addOrRemoveNumber(Keywords.MULTIPLE_OF, multipleOf)
    return this
  }

  override fun exclusiveMinimum(exclusiveMinimum: Number): JsonSchemaBuilder {
    return numberExclusiveLimit(Keywords.MINIMUM, { LimitKeyword.minimumKeyword() }, exclusiveMinimum)
  }

  // #######################################################
  // ARRAY KEYWORDS
  // @see ArrayKeywords
  // #######################################################

  override fun minimum(minimum: Number): JsonSchemaBuilder {
    return numberLimit(Keywords.MINIMUM, { LimitKeyword.minimumKeyword() }, minimum)
  }

  override fun exclusiveMaximum(exclusiveMaximum: Number): JsonSchemaBuilder {
    return numberExclusiveLimit(Keywords.MAXIMUM, { LimitKeyword.maximumKeyword() }, exclusiveMaximum)
  }

  override fun maximum(maximum: Number): JsonSchemaBuilder {
    return numberLimit(Keywords.MAXIMUM, { LimitKeyword.maximumKeyword() }, maximum)
  }

  override fun needsUniqueItems(needsUniqueItems: Boolean): JsonSchemaBuilder {
    if (needsUniqueItems) {
      return this.addOrRemoveBoolean(Keywords.UNIQUE_ITEMS, true)
    } else {
      keywords.remove(Keywords.UNIQUE_ITEMS)
      return this
    }
  }

  override fun maxItems(maxItems: Int): JsonSchemaBuilder {
    return this.addOrRemoveNumber(Keywords.MAX_ITEMS, maxItems)
  }

  override fun minItems(minItems: Int): JsonSchemaBuilder {
    return this.addOrRemoveNumber(Keywords.MIN_ITEMS, minItems)
  }

  override fun containsSchema(containsSchema: SchemaBuilder<*>): JsonSchemaBuilder {
    return this.addOrRemoveSchema(Keywords.CONTAINS, containsSchema)
  }

  override fun noAdditionalItems(): JsonSchemaBuilder {
    return updateKeyword(Keywords.ITEMS, { ItemsKeyword() }) { itemsKeyword ->
      itemsKeyword.copy(additionalItemSchema = buildSubSchema(falseSchemaBuilder(), ADDITIONAL_ITEMS))
    }
  }

  override fun schemaOfAdditionalItems(schemaOfAdditionalItems: SchemaBuilder<*>): JsonSchemaBuilder {
    return updateKeyword(Keywords.ITEMS, { ItemsKeyword() }) { itemsKeyword ->
      itemsKeyword.copy(additionalItemSchema = buildSubSchema(schemaOfAdditionalItems, Keywords.ADDITIONAL_ITEMS))
    }
  }

  override fun itemSchemas(itemSchemas: List<SchemaBuilder<*>>): JsonSchemaBuilder {
    return updateKeyword(Keywords.ITEMS,
        { ItemsKeyword() },
        { it.copy(indexedSchemas = buildSubSchemas(itemSchemas, Keywords.ITEMS)) })
  }

  override fun itemSchema(itemSchema: SchemaBuilder<*>): JsonSchemaBuilder {
    return updateKeyword(Keywords.ITEMS,
        { ItemsKeyword() },
        { it.copy(indexedSchemas = it.indexedSchemas + buildSubSchema(itemSchema, ITEMS, it.indexedSchemas.size)) })
  }

  override fun allItemSchema(allItemSchema: SchemaBuilder<*>): JsonSchemaBuilder {
    return updateKeyword(Keywords.ITEMS, { ItemsKeyword() }) { itemsKeyword ->
      val updated = buildSubSchema(allItemSchema, Keywords.ITEMS)
      itemsKeyword.copy(allItemSchema = updated)
    }
  }

  override fun notSchema(notSchema: SchemaBuilder<*>): JsonSchemaBuilder {
    return this.addOrRemoveSchema(Keywords.NOT, notSchema)
  }

  override fun enumValues(enumValues: kotlinx.serialization.json.JsonArray): JsonSchemaBuilder {
    return this.addOrRemoveJsonArray(Keywords.ENUM, enumValues)
  }

  override fun constValueString(constValue: String): JsonSchemaBuilder {
    return this.constValue(constValue.toJsonLiteral())
  }

  override fun constValueInt(constValue: Int): JsonSchemaBuilder {
    return this.constValue(constValue.toJsonLiteral())
  }

  override fun constValueDouble(constValue: Double): JsonSchemaBuilder {
    return this.constValue(constValue.toJsonLiteral())
  }

  override fun constValue(constValue: JsonElement): JsonSchemaBuilder {
    return addOrRemoveJsonElement(Keywords.CONST, constValue)
  }

  override fun oneOfSchemas(oneOfSchemas: Collection<SchemaBuilder<*>>): JsonSchemaBuilder {
    return addOrRemoveSchemaList(Keywords.ONE_OF, oneOfSchemas)
  }

  override fun oneOfSchema(oneOfSchema: SchemaBuilder<*>): JsonSchemaBuilder {
    return addSchemaToList(Keywords.ONE_OF, oneOfSchema)
  }

  override fun anyOfSchemas(anyOfSchemas: Collection<SchemaBuilder<*>>): JsonSchemaBuilder {
    return addOrRemoveSchemaList(Keywords.ANY_OF, anyOfSchemas)
  }

  override fun anyOfSchema(anyOfSchema: SchemaBuilder<*>): JsonSchemaBuilder {
    return addSchemaToList(Keywords.ANY_OF, anyOfSchema)
  }

  // #######################################################
  // NUMBER KEYWORDS
  // @see NumberKeywords
  // #######################################################

  override fun allOfSchemas(allOfSchemas: Collection<SchemaBuilder<*>>): JsonSchemaBuilder {
    return addOrRemoveSchemaList(Keywords.ALL_OF, allOfSchemas)
  }

  override fun allOfSchema(allOfSchema: SchemaBuilder<*>): JsonSchemaBuilder {
    return addSchemaToList(Keywords.ALL_OF, allOfSchema)
  }

  override fun ifSchema(ifSchema: SchemaBuilder<*>): JsonSchemaBuilder {
    return addOrRemoveSchema(IF, ifSchema)
  }

  override fun thenSchema(thenSchema: SchemaBuilder<*>): JsonSchemaBuilder {
    return addOrRemoveSchema(THEN, thenSchema)
  }

  override fun elseSchema(elseSchema: SchemaBuilder<*>): JsonSchemaBuilder {
    return addOrRemoveSchema(ELSE, elseSchema)
  }

  // #######################################################
  // Any KEYWORDS
  // @see ObjectKeywords
  // #######################################################

  override fun <X : JsonSchemaKeyword<*>> keyword(keyword: KeywordInfo<X>, keywordValue: X): JsonSchemaBuilder {
    keywords[keyword] = keywordValue
    return this
  }

  fun <X : JsonSchemaKeyword<*>> getKeyword(keyword: KeywordInfo<X>): X? {
    @Suppress("unchecked_cast")
    return keywords[keyword] as X?
  }

  override fun extraProperty(propertyName: String, JsonElement: JsonElement): JsonSchemaBuilder {
    this.extraProperties[propertyName] = JsonElement
    return this
  }

  override fun build(itemsLocation: SchemaLocation?, report: LoadingReport): Schema {
    // Use the location provided during building as an override
    var loc: SchemaLocation = itemsLocation ?: this.location
    if (id != null) {
      loc = loc.withId(this.id!!)
    }

    val finalLocation = loc

    val thisSchemaURI = finalLocation.uniqueURI

    if (schemaFactory != null) {
      val cachedSchema = schemaFactory!!.findLoadedSchema(thisSchemaURI)
      if (cachedSchema != null) {
        return cachedSchema
      }
    }

    return when {
      this.ref != null -> RefSchemaImpl(refURI = this.refURI!!,
          factory = schemaFactory,
          currentDocument = currentDocument,
          location = finalLocation,
          report = report)
      else -> Draft7SchemaImpl(finalLocation, this.keywords, this.extraProperties)
    }
  }

  override fun build(): Schema = this.build(block = {})
  override fun build(block: JsonSchemaBuilder.() -> Unit): Schema {
    this.block()
    val location: SchemaLocation = @Suppress("USELESS_ELVIS")
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

  override fun withLoadingReport(report: LoadingReport): JsonSchemaBuilder {
    this.loadingReport = report
    return this
  }

  override fun withSchemaLoader(factory: SchemaLoader): JsonSchemaBuilder {
    this.schemaFactory = factory
    return this
  }

  override fun withCurrentDocument(currentDocument: kotlinx.serialization.json.JsonObject): JsonSchemaBuilder {
    this.currentDocument = currentDocument
    return this
  }

  fun clearAllOfSchemas(): JsonSchemaBuilder {
    return addOrRemoveSchemaList(Keywords.ALL_OF, null)
  }

  fun clearAnyOfSchemas(): JsonSchemaBuilder {
    return addOrRemoveSchemaList(Keywords.ANY_OF, null)
  }

  fun clearOneOfSchemas(): JsonSchemaBuilder {
    return addOrRemoveSchemaList(Keywords.ONE_OF, null)
  }

  fun numberLimit(keyword: KeywordInfo<LimitKeyword>, newInstance: () -> LimitKeyword, limit: Number?): JsonSchemaBuilder {
    return updateKeyword(keyword, newInstance) { limitKeyword ->
      return@updateKeyword when {
        limit == null && !limitKeyword.isExclusive -> null
        else -> limitKeyword.copy(limit = limit)
      }
    }
  }

  // #######################################################
  // STRING KEYWORDS
  // @see StringKeywords
  // #######################################################

  fun numberExclusiveLimit(keyword: KeywordInfo<LimitKeyword>, newInstance: () -> LimitKeyword, exclusiveLimit: Number?): JsonSchemaBuilder {
    return updateKeyword(keyword, newInstance) { limitKeyword ->
      return@updateKeyword when {
        exclusiveLimit == null && limitKeyword.limit == null -> null
        else -> limitKeyword.copy(exclusiveLimit = exclusiveLimit)
      }
    }
  }

  fun addOrRemoveSchemaList(keyword: KeywordInfo<SchemaListKeyword>, schemas: Collection<SchemaBuilder<*>>?): JsonSchemaBuilder {
    return updateKeyword(keyword, { SchemaListKeyword() }) { listKeyword ->
      schemas?.run {
        val built = buildSubSchemas(schemas, keyword)
        return@updateKeyword listKeyword + built
      }
    }
  }

  fun addOrRemoveSchema(keyword: KeywordInfo<SingleSchemaKeyword>, schema: SchemaBuilder<*>?): JsonSchemaBuilder {
    if (schema == null) {
      keywords.remove(keyword)
    } else {
      val built = buildSubSchema(schema, keyword)
      keywords.put(keyword, SingleSchemaKeyword(built))
    }

    return this
  }

  private fun <X : JsonSchemaKeyword<*>> getKeyword(keyword: KeywordInfo<X>, defaultValue: () -> X): X {
    @Suppress("UNCHECKED_CAST")
    return keywords.getOrElse(keyword) { defaultValue() } as X
  }

  // #######################################################
  // HELPER FUNCTIONS
  // #######################################################

  /**
   * Updates a keyword by providing an update function.  This method takes care of updating or pruning the appropriate keys in the
   * [.keywords] map.
   *
   * @param keyword The keyword being updated
   * @param newInstanceFn A supplier that produces a new blank instance of the keyword value
   * @param updateFn A function that takes in the current keyword value, and returns an updated value.
   * @param <K> Type parameter for the keyword in question.  Enforces that the key matches the value
   * @return Self-reference for chaining.
  </K> */
  protected fun <K : JsonSchemaKeyword<*>> updateKeyword(keyword: KeywordInfo<K>,
                                                         newInstanceFn: () -> K,
                                                         updateFn: (K) -> K?): JsonSchemaBuilder {
    val keywordValue = getKeyword(keyword, newInstanceFn)
    val updatedKeyword = updateFn(keywordValue)
    if (updatedKeyword == null) {
      keywords.remove(keyword)
    } else {
      keywords[keyword] = updatedKeyword
    }
    return this
  }

  protected fun setOrRemoveId(id: URI): JsonSchemaBuilder {
    if (!removeIfNecessary<Any>(DOLLAR_ID, id)) {
      val keywordValue = IdKeyword(id)
      keywords[DOLLAR_ID] = keywordValue
    }
    return this
  }

  protected fun addOrRemoveString(keyword: KeywordInfo<StringKeyword>, value: String): JsonSchemaBuilder {
    if (!removeIfNecessary<Any>(keyword, value)) {
      val keywordValue = StringKeyword(value)
      keywords.put(keyword, keywordValue)
    }
    return this
  }

  protected fun <X> removeIfNecessary(keyword: KeywordInfo<*>, value: X?): Boolean {
    if (value == null) {
      keywords.remove(keyword)
    }
    return value == null
  }

  protected fun addOrRemoveURI(keyword: KeywordInfo<URIKeyword>, value: URI?): JsonSchemaBuilder {
    if (!removeIfNecessary<Any>(keyword, value)) {
      val keywordValue = URIKeyword(value!!)
      keywords.put(keyword, keywordValue)
    }
    return this
  }

  protected fun addOrRemoveJsonArray(keyword: KeywordInfo<JsonArrayKeyword>, value: kotlinx.serialization.json.JsonArray): JsonSchemaBuilder {
    if (!removeIfNecessary<Any>(keyword, value)) {
      val keywordValue = JsonArrayKeyword(value)
      keywords.put(keyword, keywordValue)
    }
    return this
  }

  protected fun addOrRemoveNumber(keyword: KeywordInfo<NumberKeyword>, value: Number): JsonSchemaBuilder {
    if (!removeIfNecessary<Any>(keyword, value)) {
      val keywordValue = NumberKeyword(value)
      keywords.put(keyword, keywordValue)
    }
    return this
  }

  protected fun addOrRemoveBoolean(keyword: KeywordInfo<BooleanKeyword>, bool: Boolean): JsonSchemaBuilder {
    if (!removeIfNecessary<Any>(keyword, bool)) {
      val keywordValue = BooleanKeyword(bool)
      keywords.put(keyword, keywordValue)
    }
    return this
  }

  protected fun addOrRemoveJsonElement(keyword: KeywordInfo<JsonValueKeyword>, value: JsonElement): JsonSchemaBuilder {
    if (!removeIfNecessary<Any>(keyword, value)) {
      val keywordValue = JsonValueKeyword(value)
      keywords.put(keyword, keywordValue)
    }
    return this
  }

  protected fun addSchemaToList(keyword: KeywordInfo<SchemaListKeyword>, schema: SchemaBuilder<*>): JsonSchemaBuilder {
    return updateKeyword(keyword, { SchemaListKeyword() }) { listKeyword ->
      listKeyword + buildSubSchema(schema, keyword, listKeyword)
    }
  }

  @Suppress("unchecked_cast")
  protected fun putKeywordSchema(keyword: KeywordInfo<SchemaMapKeyword>,
                                 key: String,
                                 value: SchemaBuilder<*>): JsonSchemaBuilder {
    val schema = buildSubSchema(value, keyword, key)
    return updateKeyword(keyword, { SchemaMapKeyword() }) { schemaMap ->
      return@updateKeyword schemaMap + (key to schema)
    }
  }

  private fun buildSubSchema(toBuild: SchemaBuilder<*>, keyword: KeywordInfo<*>): Schema {
    val childLocation = this.location.child(keyword)
    return toBuild.build(childLocation, loadingReport)
  }

  private fun buildSubSchema(toBuild: SchemaBuilder<*>, keyword: KeywordInfo<*>, path: String, vararg paths: String): Schema {
    val childLocation = this.location.child(keyword).child(path).child(*paths)
    return toBuild.build(childLocation, loadingReport)
  }

  private fun buildSubSchema(toBuild: SchemaBuilder<*>, keyword: KeywordInfo<*>, idx: Int): Schema {
    val childLocation = this.location.child(keyword).child(idx)
    return toBuild.build(childLocation, loadingReport)
  }

  private fun buildSubSchema(toBuild: SchemaBuilder<*>, keyword: KeywordInfo<*>, list: SchemaListKeyword): Schema {
    val childLocation = this.location.child(keyword).child(list.subschemas.size)
    return toBuild.build(childLocation, loadingReport)
  }

  private fun buildSubSchemas(toBuild: Collection<SchemaBuilder<*>>?, keyword: KeywordInfo<*>): List<Schema> {
    var idx = 0
    val childPath = this.location.child(keyword)
    return toBuild!!.map { builder -> builder.build(childPath.child(idx++), loadingReport) }
  }

  // #######################################################
  // LAZY GETTERS
  // #######################################################

  override fun equals(other: Any?): Boolean {
    return when {
      this === other -> true
      other !is JsonSchemaBuilder -> false
      this.keywords == other.keywords -> true
      else -> false
    }
  }

  override fun hashCode(): Int = hashKode(keywords)
  override fun propertyNameSchema(block: SchemaBuilder<*>.() -> Unit): JsonSchemaBuilder = propertyNameSchema(jsonschemaBuilder(init = block))
  override fun patternProperty(pattern: String, block: SchemaBuilder<*>.() -> Unit): JsonSchemaBuilder = patternProperty(pattern, jsonschemaBuilder(init = block))
  override fun patternProperty(pattern: Pattern, block: SchemaBuilder<*>.() -> Unit): JsonSchemaBuilder = patternProperty(pattern, jsonschemaBuilder(init = block))
  override fun containsSchema(block: SchemaBuilder<*>.() -> Unit): JsonSchemaBuilder = containsSchema(jsonschemaBuilder(init = block))
  override fun schemaOfAdditionalItems(block: SchemaBuilder<*>.() -> Unit): JsonSchemaBuilder = schemaOfAdditionalItems(jsonschemaBuilder(init = block))
  override fun itemSchema(block: SchemaBuilder<*>.() -> Unit): JsonSchemaBuilder = itemSchema(jsonschemaBuilder(init = block))
  override fun allItemSchema(block: SchemaBuilder<*>.() -> Unit): JsonSchemaBuilder = allItemSchema(jsonschemaBuilder(init = block))
  override fun notSchema(block: SchemaBuilder<*>.() -> Unit): JsonSchemaBuilder = this.notSchema(jsonschemaBuilder(init = block))
  override fun enumValues(vararg enumValues: Any?): JsonSchemaBuilder = apply {
    this.enumValues(enumValues.toList().toJsonArray())
  }
  override fun oneOfSchema(block: SchemaBuilder<*>.() -> Unit): JsonSchemaBuilder = oneOfSchema(jsonschemaBuilder(init = block))
  override fun anyOfSchema(block: SchemaBuilder<*>.() -> Unit): JsonSchemaBuilder = anyOfSchema(jsonschemaBuilder(init = block))
  override fun allOfSchema(block: SchemaBuilder<*>.() -> Unit): JsonSchemaBuilder = allOfSchema(jsonschemaBuilder(init = block))
  override fun ifSchema(block: SchemaBuilder<*>.() -> Unit): JsonSchemaBuilder = ifSchema(jsonschemaBuilder(init = block))
  override fun thenSchema(block: SchemaBuilder<*>.() -> Unit): JsonSchemaBuilder = thenSchema(jsonschemaBuilder(init = block))
  override fun elseSchema(block: SchemaBuilder<*>.() -> Unit): JsonSchemaBuilder = elseSchema(jsonschemaBuilder(init = block))
}
