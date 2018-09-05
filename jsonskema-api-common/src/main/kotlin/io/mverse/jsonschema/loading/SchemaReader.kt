package io.mverse.jsonschema.loading

import io.mverse.jsonschema.JsonValueWithPath.Companion.fromJsonValue
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.keyword.KeywordLoader
import io.mverse.jsonschema.keyword.Keyword
import io.mverse.jsonschema.keyword.KeywordInfo
import kotlinx.io.InputStream
import kotlinx.serialization.internal.readToByteBuffer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonTreeParser
import lang.Name
import lang.URI
import lang.toString

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
  fun withPreloadedDocument(schemaObject: kotlinx.serialization.json.JsonObject): SchemaReader = this + schemaObject
  fun withStrictValidation(vararg versions: JsonSchemaVersion): SchemaReader

  fun <K : Keyword<*>> withCustomKeywordLoader(keyword: KeywordInfo<K>,
                                               keywordExtractor: KeywordLoader<K>): SchemaReader

  operator fun <K : Keyword<*>> plus(pair: Pair<KeywordInfo<K>, KeywordLoader<K>>): SchemaReader

  @Name("readSchemaFromStream")
  fun readSchema(inputStream: InputStream): Schema {
    try {
      return readSchema(inputStream.parseJsonObject())
    } finally {
      inputStream.close()
    }
  }

  @Name("readSchemaFromURI")
  fun readSchema(schemaURI: URI): Schema {
    val existing = loader.findLoadedSchema(schemaURI)
    return when (existing) {
      null-> {
        val fetchedDocument = documentClient.findLoadedDocument(schemaURI)
            ?: documentClient.fetchDocument(schemaURI)
        readSchema(fetchedDocument)
      }
      else-> existing
    }
  }

  @Name("readSchemaFromJsonObject")
  fun readSchema(jsonObject: JsonObject, loadingReport: LoadingReport = LoadingReport()): Schema {
    val jsonDocument = fromJsonValue(jsonObject)
    return loader.schemaBuilder(jsonDocument, loadingReport).build()
  }

  @Name("readSchemaFromString")
  fun readSchema(inputJson: String): Schema {
    return readSchema(inputJson.parseJsonObject())
  }

  operator fun plus(document: JsonObject):SchemaReader
  operator fun plus(document:InputStream):SchemaReader = this + document.parseJsonObject()
  operator fun plus(document:String):SchemaReader = this + document.parseJsonObject()
}

fun String.parseJsonObject(): JsonObject = JsonTreeParser(this).readFully().jsonObject
fun String.parseJson():JsonElement = JsonTreeParser(this).readFully()

fun InputStream.parseJsonObject(): JsonObject = readFully().parseJsonObject()
fun InputStream.parseJson():JsonElement = readFully().parseJson()

fun InputStream.readFully():String {
  try {
    return readToByteBuffer(this.available()).array().toString("UTF-8")
  } finally {
    close()
  }
}
