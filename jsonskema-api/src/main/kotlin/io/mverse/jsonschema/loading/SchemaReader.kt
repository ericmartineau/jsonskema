package io.mverse.jsonschema.loading

import io.mverse.jsonschema.JsonValueWithPath.Companion.fromJsonValue
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.keyword.CustomKeywordLoader
import io.mverse.jsonschema.keyword.JsonSchemaKeyword
import io.mverse.jsonschema.keyword.KeywordInfo
import kotlinx.io.InputStream
import kotlinx.serialization.internal.readToByteBuffer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonTreeParser
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

  fun withDocumentClient(jsonDocumentClient: JsonDocumentClient): SchemaReader
  fun withPreloadedDocument(schemaObject: kotlinx.serialization.json.JsonObject): SchemaReader = this + schemaObject
  fun withStrictValidation(vararg versions: JsonSchemaVersion): SchemaReader

  fun <K : JsonSchemaKeyword<*>> withCustomKeywordLoader(keyword: KeywordInfo<K>,
                                                         keywordExtractor: CustomKeywordLoader<K>): SchemaReader

  fun readSchema(inputStream: InputStream): Schema {
    try {
      return readSchema(inputStream.parseJsonObject())
    } finally {
      inputStream.close()
    }
  }

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

  fun readSchema(jsonObject: kotlinx.serialization.json.JsonObject): Schema {
    return readSchema(jsonObject, LoadingReport())
  }

  fun readSchema(jsonObject: kotlinx.serialization.json.JsonObject, loadingReport: LoadingReport): Schema {
    val jsonDocument = fromJsonValue(jsonObject)
    return loader.schemaBuilder(jsonDocument, loadingReport).build()
  }

  fun readSchema(inputJson: String): Schema {
    return readSchema(inputJson.parseJsonObject())
  }

  operator fun plus(document: kotlinx.serialization.json.JsonObject):SchemaReader
  operator fun plus(document:InputStream):SchemaReader = this + document.parseJsonObject()
  operator fun plus(document:String):SchemaReader = this + document.parseJsonObject()
}


fun String.parseJsonObject(): kotlinx.serialization.json.JsonObject = JsonTreeParser(this).readFully().jsonObject
fun String.parseJson():JsonElement = JsonTreeParser(this).readFully()

fun InputStream.parseJsonObject(): kotlinx.serialization.json.JsonObject = readFully().parseJsonObject()
fun InputStream.parseJson():JsonElement = readFully().parseJson()

fun InputStream.readFully():String {
  try {
    return readToByteBuffer(this.available()).array().toString("UTF-8")
    //
  } finally {
    close()
  }
}
