package io.mverse.jsonschema.loading

import io.mverse.jsonschema.JsonValueWithPath.Companion.fromJsonValue
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.keyword.Keyword
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.KeywordLoader
import kotlinx.io.InputStream
import kotlinx.serialization.internal.readToByteBuffer
import lang.Name
import lang.json.JsrObject
import lang.json.JsrValue
import lang.json.parseJsrValue
import lang.net.URI
import lang.string.Charsets
import lang.string.toString

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

  @Name("readSchemaFromStream")
  fun readSchema(inputStream: InputStream): Schema {
    try {
      return readSchema(inputStream.parseJsrObject())
    } finally {
      inputStream.close()
    }
  }

  @Name("readSchemaFromURI")
  fun readSchema(schemaURI: URI): Schema {
    val existing = loader.findLoadedSchema(schemaURI)
    return when (existing) {
      null -> {
        val fetchedDocument = documentClient.findLoadedDocument(schemaURI)
            ?: documentClient.fetchDocument(schemaURI)
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
  operator fun plus(document: InputStream): SchemaReader = this + document.parseJsrObject()
  operator fun plus(document: String): SchemaReader = this + document.parseJsrObject()
}

fun String.parseJsrObject(): JsrObject = parseJsrValue(this) as JsrObject
fun String.parseJsrJson(): JsrValue = parseJsrValue(this)

fun InputStream.parseJsrObject(): JsrObject = readFully().parseJsrObject()
fun InputStream.parseJsrJson(): JsrValue = readFully().parseJsrJson()

fun InputStream.readFully(): String {
  try {
    return readToByteBuffer(this.available()).array().toString(Charsets.UTF_8)
  } finally {
    close()
  }
}
