package io.mverse.jsonschema.loading

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaBuilder
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.keyword.CustomKeywordLoader
import io.mverse.jsonschema.keyword.JsonSchemaKeyword
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.loading.keyword.versions.KeywordDigesterImpl
import io.mverse.jsonschema.loading.reference.DefaultJsonDocumentClient
import io.mverse.jsonschema.loading.reference.SchemaCache
import io.mverse.jsonschema.utils.JsonUtils
import kotlinx.serialization.json.JsonObject
import lang.URI

/**
 * Schema factories are responsible for extracting values from a json-object to produce an immutable [Schema]
 * instance.  This also includes looking up any referenced schemas.
 */
data class SchemaLoaderImpl(
    val schemaCache: SchemaCache = SchemaCache(),
    override val documentClient: JsonDocumentClient = DefaultJsonDocumentClient(schemaCache = schemaCache),
    val additionalKeywords: List<KeywordDigester<*>> = emptyList(),
    val versions: Set<JsonSchemaVersion> = emptySet(),
    val isStrict: Boolean = false) : SchemaReader, SchemaLoader {

  override val loader: SchemaLoader = this

  val fragmentLoader: SubSchemaLoader = SubSchemaLoader(extraKeywordLoaders = additionalKeywords,
      defaultVersions = versions, strict = isStrict, schemaLoader = this)
  val refSchemaLoader: RefSchemaLoader = RefSchemaLoader(documentClient = this.documentClient, schemaLoader = this)

  // #############################################################
  // ########  LOADING SCHEMAS/SUBSCHEMAS FROM JSON    ###########
  // #############################################################

  override fun loadRefSchema(referencedFrom: Schema, refURI: URI, currentDocument: JsonObject?, report: LoadingReport): Schema {
    return refSchemaLoader.loadRefSchema(referencedFrom, refURI, currentDocument, report)
  }

  override fun loadSubSchema(schemaJson: JsonValueWithPath, inDocument: kotlinx.serialization.json.JsonObject, loadingReport: LoadingReport): Schema {
    return schemaCache.getSchema(schemaJson.location)
        ?: subSchemaBuilder(schemaJson, inDocument, loadingReport).build()
            .apply { schemaCache.cacheSchema(this) }
  }

  // #############################################################
  // #######  LOADING  & CREATING SUBSCHEMA FACTORIES  ###########
  // #############################################################

  override fun subSchemaBuilder(schemaJson: JsonValueWithPath, inDocument: kotlinx.serialization.json.JsonObject, loadingReport: LoadingReport): SchemaBuilder<*> {
    return fragmentLoader.subSchemaBuilder(schemaJson, inDocument, loadingReport)
  }

  // #############################################################
  // ############  FINDING/STORING LOADED SCHEMAS  ###############
  // #############################################################

  override fun findLoadedSchema(schemaLocation: URI): Schema? {
    return schemaCache.getSchema(schemaLocation)
  }

  override fun withPreloadedSchema(schema: Schema): SchemaLoader {
    schemaCache.cacheSchema(schema)
    return this
  }

  override fun withDocumentClient(documentClient: JsonDocumentClient): SchemaLoaderImpl {
    return this.copy(documentClient = documentClient)
  }

  // #############################################################
  // #####  FACTORY METHODS (FOR CUSTOMIZING THE LOADER)   #######
  // #############################################################

  override fun withPreloadedDocument(schemaObject: JsonObject): SchemaReader {
    JsonUtils.extractIdFromObject(schemaObject)?.also { id ->
      documentClient.registerLoadedDocument(id, schemaObject)
    }
    return this
  }

  override operator fun plus(document: JsonObject): SchemaReader {
    JsonUtils.extractIdFromObject(document)?.also { id ->
      documentClient.registerLoadedDocument(id, document)
    }
    return this
  }

  override operator fun plusAssign(preloadedSchema: JsonObject) {
    JsonUtils.extractIdFromObject(preloadedSchema)?.also { id ->
      documentClient.registerLoadedDocument(id, preloadedSchema)
    }
  }

  override fun withStrictValidation(vararg versions: JsonSchemaVersion): SchemaReader {
    check(versions.isNotEmpty()) { " must not be blank" }

    return this.copy(isStrict = true, versions = versions.toSet())
  }

  override fun <K : JsonSchemaKeyword<*>> withCustomKeywordLoader(keyword: KeywordInfo<K>, keywordExtractor: CustomKeywordLoader<K>): SchemaReader {
    val newKeywordDigester = KeywordDigesterImpl(keyword, keywordExtractor)
    return this.copy(additionalKeywords = this.additionalKeywords + newKeywordDigester)
  }

  override operator fun <K : JsonSchemaKeyword<*>> plus(pair: Pair<KeywordInfo<K>, CustomKeywordLoader<K>>): SchemaReader {
    return withCustomKeywordLoader(pair.first, pair.second)
  }
}

fun <K : JsonSchemaKeyword<*>> customKeyword(keyword: KeywordInfo<K>, keywordExtractor: (JsonValueWithPath) -> K):
    Pair<KeywordInfo<K>, CustomKeywordLoader<K>> {
  return keyword to object : CustomKeywordLoader<K> {
    override fun loadKeywordFromJsonValue(jsonValue: JsonValueWithPath): K? = keywordExtractor(jsonValue)
  }
}

