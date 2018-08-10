package io.mverse.jsonschema.builder

import io.mverse.jsonschema.DraftSchema
import io.mverse.jsonschema.MutableKeywordContainer
import io.mverse.jsonschema.RefSchema
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaBuilder
import io.mverse.jsonschema.SchemaLocation
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.impl.Draft7SchemaImpl
import io.mverse.jsonschema.impl.RefSchemaImpl
import io.mverse.jsonschema.jsonschema
import io.mverse.jsonschema.keyword.BooleanKeyword
import io.mverse.jsonschema.keyword.DependenciesKeyword
import io.mverse.jsonschema.keyword.IdKeyword
import io.mverse.jsonschema.keyword.ItemsKeyword
import io.mverse.jsonschema.keyword.JsonArrayKeyword
import io.mverse.jsonschema.keyword.JsonSchemaKeyword
import io.mverse.jsonschema.keyword.JsonValueKeyword
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.Keywords.Companion.ADDITIONAL_ITEMS
import io.mverse.jsonschema.keyword.Keywords.Companion.COMMENT
import io.mverse.jsonschema.keyword.Keywords.Companion.DOLLAR_ID
import io.mverse.jsonschema.keyword.Keywords.Companion.ELSE
import io.mverse.jsonschema.keyword.Keywords.Companion.IF
import io.mverse.jsonschema.keyword.Keywords.Companion.ITEMS
import io.mverse.jsonschema.keyword.Keywords.Companion.PROPERTIES
import io.mverse.jsonschema.keyword.Keywords.Companion.READ_ONLY
import io.mverse.jsonschema.keyword.Keywords.Companion.REF
import io.mverse.jsonschema.keyword.Keywords.Companion.SCHEMA
import io.mverse.jsonschema.keyword.Keywords.Companion.THEN
import io.mverse.jsonschema.keyword.Keywords.Companion.WRITE_ONLY
import io.mverse.jsonschema.keyword.LimitKeyword
import io.mverse.jsonschema.keyword.NumberKeyword
import io.mverse.jsonschema.keyword.SchemaKeyword
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
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import lang.Pattern
import lang.URI
import lang.UUID
import lang.hashKode
import lang.json.toJsonLiteral

open class JsonSchemaBuilder(
    keywords: MutableMap<KeywordInfo<*>, JsonSchemaKeyword<*>> = mutableMapOf(),
    private val extraProperties: MutableMap<String, JsonElement> = mutableMapOf(),
    private val location: SchemaLocation = SchemaPaths.fromNonSchemaSource(UUID.randomUUID()),
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

  override val id: URI? get() = getKeyword(Keywords.DOLLAR_ID)?.keywordValue
  override var ref: URI?
    get() = getKeyword(Keywords.REF)?.keywordValue
    set(ref) { addOrRemoveURI(REF, ref) }

  override fun withSchema(): JsonSchemaBuilder = apply { keywords[SCHEMA] = SchemaKeyword() }
  override fun withoutSchema(): JsonSchemaBuilder = apply { keywords -= SCHEMA }
  override fun ref(ref: URI): JsonSchemaBuilder = apply {this.ref = ref}
  override fun ref(ref: String): JsonSchemaBuilder = apply {this.ref = URI(ref)}

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

  fun propertySchema(propertySchemaKey: String, block: SchemaBuilder<*>.()->Unit): JsonSchemaBuilder {
    this.putKeywordSchema(Keywords.PROPERTIES, propertySchemaKey, jsonschema(init = block))
    return this
  }

  override fun updatePropertySchema(propertyName: String,
                                    updater: (SchemaBuilder<*>) -> SchemaBuilder<*>): JsonSchemaBuilder {
    this.updateKeyword(PROPERTIES, { SchemaMapKeyword() }) { schemaMap ->
      val schema = schemaMap.schemas[propertyName]
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

  override fun <X : JsonSchemaKeyword<*>> keyword(keyword: KeywordInfo<X>, value: X): JsonSchemaBuilder {
    keywords[keyword] = value
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


  override fun build(location: SchemaLocation?, report: LoadingReport): Schema {
    // Use the location provided during building as an override
    var loc: SchemaLocation = location ?: this.location!!
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
      this.ref != null -> RefSchemaImpl(refURI = this.ref!!,
          factory = schemaFactory,
          currentDocument = currentDocument,
          location = finalLocation,
          report = report)
      else -> Draft7SchemaImpl(finalLocation, this.keywords, this.extraProperties)
    }
  }

  override fun build(block:JsonSchemaBuilder.()->Unit): Schema {
    this.block()
    val location: SchemaLocation = when {
      this.id == null -> this.location ?: SchemaPaths.fromBuilder(this)
      this.location != null -> this.location.withId(this.id!!)
      else -> SchemaPaths.fromId(this.id!!)
    }

    val built = build(location, loadingReport)
    if (loadingReport.hasErrors()) {
      throw SchemaLoadingException(location.jsonPointerFragment, loadingReport, built)
    }
    return built
  }

  override fun withLoadingReport(report: LoadingReport): JsonSchemaBuilder {
    this.loadingReport = report
    return this
  }

  override fun withSchemaLoader(schemaFactory: SchemaLoader): JsonSchemaBuilder {
    this.schemaFactory = schemaFactory
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
        else -> limitKeyword.copy(exclusive = exclusiveLimit)
      }
    }
  }

  fun addOrRemoveSchemaList(keyword: KeywordInfo<SchemaListKeyword>, schemas: Collection<SchemaBuilder<*>>?): JsonSchemaBuilder {
    return updateKeyword(keyword, { SchemaListKeyword() }) { listKeyword ->
      schemas?.run {
        val built = buildSubSchemas(schemas, keyword)
        return@updateKeyword listKeyword.copy(schemas = built)
      }
    }
  }

  fun addOrRemoveSchema(keyword: KeywordInfo<SingleSchemaKeyword>, schema: SchemaBuilder<*>?): JsonSchemaBuilder {
    if (schema == null) {
      keywords.remove(keyword)
    } else {
      val built = buildSubSchema(schema!!, keyword)
      keywords.put(keyword, SingleSchemaKeyword(built))
    }

    return this
  }

  //  fun putAllKeywordSchemas(keyword: KeywordInfo<SchemaMapKeyword>, schemas: Map<String, SchemaBuilder<*>>?): JsonSchemaBuilder {
  //    if (schemas == null || schemas.isEmpty()) {
  //      keywords.remove(keyword)
  //      return this
  //    } else {
  //      val builtSchemas = schemas.entrySet().stream()
  //          .collect(Collectors.toMap(
  //              { e -> e.getKey() },
  //              { e -> buildSubSchema(e.getValue(), keyword, e.getKey()) }
  //          ))
  //      return updateKeyword(keyword, { SchemaMapKeyword.empty() }, { builder ->
  //        builder.toBuilder()
  //            .schemas(builtSchemas)
  //            .build()
  //      })
  //    }
  //  }

  private fun <X : JsonSchemaKeyword<*>> getKeyword(keyword: KeywordInfo<X>, defaultValue: () -> X): X {
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
    check(this.location != null) { "Location cannot be null" }
    val childLocation = this.location!!.child(keyword)
    return toBuild.build(childLocation, loadingReport)
  }

  private fun buildSubSchema(toBuild: SchemaBuilder<*>, keyword: KeywordInfo<*>, path: String, vararg paths: String): Schema {
    check(this.location != null) { "Location cannot be null" }
    val childLocation = this.location!!.child(keyword).child(path).child(*paths)
    return toBuild.build(childLocation, loadingReport)
  }

  private fun buildSubSchema(toBuild: SchemaBuilder<*>, keyword: KeywordInfo<*>, idx: Int): Schema {
    check(this.location != null) { "Location cannot be null" }
    val childLocation = this.location!!.child(keyword).child(idx)
    return toBuild.build(childLocation, loadingReport)
  }

  private fun buildSubSchema(toBuild: SchemaBuilder<*>, keyword: KeywordInfo<*>, list: SchemaListKeyword): Schema {
    check(this.location != null) { "Location cannot be null" }
    val childLocation = this.location!!.child(keyword).child(list.subschemas.size)
    return toBuild.build(childLocation, loadingReport)
  }

  private fun buildSubSchemas(toBuild: Collection<SchemaBuilder<*>>?, keyword: KeywordInfo<*>): List<Schema> {
    check(this.location != null) { "Location cannot be null" }
    var idx = 0
    val childPath = this.location!!.child(keyword)
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
}
