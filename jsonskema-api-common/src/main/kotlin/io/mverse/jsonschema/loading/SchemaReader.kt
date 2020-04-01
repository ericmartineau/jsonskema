package io.mverse.jsonschema.loading

import io.mverse.jsonschema.JsonValueWithPath.Companion.fromJsonValue
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.keyword.Keyword
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.KeywordLoader
import io.mverse.jsonschema.utils.asInput
import kotlinx.io.InputStream
import kotlinx.io.core.Input
import kotlinx.io.core.readText
import kotlinx.serialization.InternalSerializationApi
import lang.Name
import lang.json.JsrObject
import lang.json.JsrValue
import lang.json.parseJsrValue
import lang.net.URI

/**
 * Main interface for reading a schema from an input source.
 */
interface SchemaReader {
  /**
   * The schemaLoader to use for the lower-level loading operations.
   */
  val loader: SchemaLoader

  val documentClient: JsonDocumentClient

  fun withDocumentClient(documentClient: JsonDocumentClient): SchemaReader
  fun withPreloadedDocument(schemaObject: lang.json.JsrObject): SchemaReader = this + schemaObject
  fun withStrictValidation(vararg versions: JsonSchemaVersion): SchemaReader

  fun <K : Keyword<*>> withCustomKeywordLoader(keyword: KeywordInfo<K>,
                                               keywordExtractor: KeywordLoader<K>): SchemaReader

  operator fun <K : Keyword<*>> plus(pair: Pair<KeywordInfo<K>, KeywordLoader<K>>): SchemaReader

  @OptIn(InternalSerializationApi::class)
  @Name("readSchemaFromStream")
  fun readSchema(inputStream: InputStream): Schema = readSchema(inputStream.asInput())

  @Name("readSchemaFromInput")
  fun readSchema(input: Input): Schema {
    try {
      return readSchema(input.parseJsrObject())
    } finally {
      input.close()
    }
  }

  @Name("readSchemaFromURI")
  fun readSchema(schemaURI: URI): Schema {
    val existing = loader.findLoadedSchema(schemaURI)
    return when (existing) {
      null -> {
        val fetchedDocument = documentClient.findLoadedDocument(schemaURI)
            ?: documentClient.fetchDocument(schemaURI).fetchedJson
        readSchema(fetchedDocument)
      }
      else -> existing
    }
  }

  @Name("readSchemaFromJsonObject")
  fun readSchema(jsonObject: JsrObject, loadingReport: LoadingReport = LoadingReport()): Schema {
    val jsonDocument = fromJsonValue(jsonObject)
    return loader.schemaBuilder(jsonDocument, loadingReport).build()
  }

  @Name("readSchemaFromString")
  fun readSchema(inputJson: String): Schema {
    return readSchema(inputJson.parseJsrObject())
  }

  operator fun plus(document: JsrObject): SchemaReader
  operator fun plus(document: Input): SchemaReader = this + document.parseJsrObject()
  operator fun plus(document: String): SchemaReader = this + document.parseJsrObject()
}

fun String.parseJsrObject(): JsrObject = parseJsrValue(this) as JsrObject
fun String.parseJsrJson(): JsrValue = parseJsrValue(this)

fun Input.parseJsrObject(): JsrObject = readText().parseJsrObject()
fun Input.parseJsrJson(): JsrValue = readText().parseJsrJson()

@OptIn(InternalSerializationApi::class) fun InputStream.parseJsrObject(): JsrObject = asInput().readText().parseJsrObject()
@OptIn(InternalSerializationApi::class) fun InputStream.parseJsrJson(): JsrValue = asInput().readText().parseJsrJson()

