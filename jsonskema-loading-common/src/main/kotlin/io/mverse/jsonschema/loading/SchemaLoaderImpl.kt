package io.mverse.jsonschema.loading

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaBuilder
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.keyword.Keyword
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.KeywordLoader
import io.mverse.jsonschema.loading.keyword.versions.KeywordDigesterImpl
import io.mverse.jsonschema.loading.reference.DefaultJsonDocumentClient
import io.mverse.jsonschema.loading.reference.SchemaCache
import io.mverse.jsonschema.utils.JsonUtils
import lang.json.JsrObject
import lang.net.URI

/**
 * This class is responsible for extracting values from a json instance to produce an immutable [Schema]
 * instance.  This also includes looking up any referenced schemas.
 */
data class SchemaLoaderImpl(
    internal val schemaCache: SchemaCache = SchemaCache(),
    override val documentClient: JsonDocumentClient = DefaultJsonDocumentClient(schemaCache = schemaCache),
    private val additionalDigesters: List<KeywordDigester<*>> = emptyList(),
    private val versions: Set<JsonSchemaVersion> = emptySet(),
    private val isStrict: Boolean = false) : SchemaReader, SchemaLoader {

  override val loader: SchemaLoader = this
  private val fragmentLoader: SubSchemaLoader = SubSchemaLoader(extraKeywordLoaders = additionalDigesters,
      defaultVersions = versions, strict = isStrict, schemaLoader = this)
  internal val refSchemaLoader: RefSchemaLoader = RefSchemaLoader(documentClient = this.documentClient, schemaLoader = this)

  // #############################################################
  // ########  LOADING SCHEMAS/SUBSCHEMAS FROM JSON    ###########
  // #############################################################

  override fun loadRefSchema(referencedFrom: Schema, refURI: URI, currentDocument: JsrObject?, report: LoadingReport): Schema {
    return refSchemaLoader.loadRefSchema(referencedFrom, refURI, currentDocument, report)
  }

  override fun loadSubSchema(schemaJson: JsonValueWithPath, inDocument: lang.json.JsrObject, loadingReport: LoadingReport): Schema {
    return schemaCache.getSchema(schemaJson.location)
        ?: subSchemaBuilder(schemaJson, inDocument, loadingReport).build()
            .apply { schemaCache.cacheSchema(this) }
  }

  // #############################################################
  // #######  LOADING  & CREATING SUBSCHEMA FACTORIES  ###########
  // #############################################################

  override fun subSchemaBuilder(schemaJson: JsonValueWithPath, inDocument: lang.json.JsrObject, loadingReport: LoadingReport): SchemaBuilder {
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

  override fun withPreloadedDocument(schemaObject: JsrObject): SchemaReader {
    JsonUtils.extractIdFromObject(schemaObject)?.also { id ->
      documentClient.registerFetchedDocument(id, schemaObject)
    }
    return this
  }

  override operator fun plus(document: JsrObject): SchemaReader {
    JsonUtils.extractIdFromObject(document)?.also { id ->
      documentClient.registerFetchedDocument(id, document)
    }
    return this
  }

  override operator fun plusAssign(preloadedSchema: JsrObject) {
    JsonUtils.extractIdFromObject(preloadedSchema)?.also { id ->
      documentClient.registerFetchedDocument(id, preloadedSchema)
    }
  }

  override fun withStrictValidation(vararg versions: JsonSchemaVersion): SchemaReader {
    check(versions.isNotEmpty()) { " must not be blank" }

    return this.copy(isStrict = true, versions = versions.toSet())
  }

  override fun <K : Keyword<*>> withCustomKeywordLoader(keyword: KeywordInfo<K>, keywordExtractor: KeywordLoader<K>): SchemaReader {
    val newKeywordDigester = KeywordDigesterImpl(keyword, keywordExtractor)
    return this.copy(additionalDigesters = this.additionalDigesters + newKeywordDigester)
  }

  override operator fun <K : Keyword<*>> plus(pair: Pair<KeywordInfo<K>, KeywordLoader<K>>): SchemaReader {
    return withCustomKeywordLoader(pair.first, pair.second)
  }
}

fun <K : Keyword<*>> customKeyword(keyword: KeywordInfo<K>, keywordExtractor: (JsonValueWithPath) -> K):
    Pair<KeywordInfo<K>, KeywordLoader<K>> {
  return keyword to object : KeywordLoader<K> {
    override fun loadKeyword(jsonValue: JsonValueWithPath): K? = keywordExtractor(jsonValue)
  }
}

