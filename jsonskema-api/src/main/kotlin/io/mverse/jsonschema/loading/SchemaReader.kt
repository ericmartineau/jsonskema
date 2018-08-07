package io.mverse.jsonschema.loading

import io.mverse.jsonschema.JsonValueWithPath.Companion.fromJsonValue
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.keyword.CustomKeywordLoader
import io.mverse.jsonschema.keyword.JsonSchemaKeyword
import io.mverse.jsonschema.keyword.KeywordInfo
import kotlinx.io.InputStream
import kotlinx.serialization.internal.readToByteBuffer
import kotlinx.serialization.json.JSON
import kotlinx.serialization.json.JsonObject
import lang.URI

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
  fun withPreloadedDocument(schemaObject: JsonObject): SchemaReader = this + schemaObject
  fun withStrictValidation(vararg versions: JsonSchemaVersion): SchemaReader
  fun <K : JsonSchemaKeyword<*>> withCustomKeywordLoader(keyword: KeywordInfo<K>,
                                                         keywordExtractor: CustomKeywordLoader<K>): SchemaReader

  fun readSchema(inputStream: InputStream): Schema {
    try {
      return readSchema(inputStream.toJsonObject())
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

  fun readSchema(jsonObject: JsonObject): Schema {
    return readSchema(jsonObject, LoadingReport())
  }

  fun readSchema(jsonObject: JsonObject, loadingReport: LoadingReport): Schema {
    val jsonDocument = fromJsonValue(jsonObject)
    return loader.schemaBuilder(jsonDocument, loadingReport).build()
  }

  fun readSchema(inputJson: String): Schema {
    return readSchema(inputJson.toJsonObject())
  }

  operator fun plus(document:JsonObject):SchemaReader
  operator fun plus(document:InputStream):SchemaReader = this + document.toJsonObject()
  operator fun plus(document:String):SchemaReader = this + document.toJsonObject()

  private fun String.toJsonObject():JsonObject = JSON.parse(this)
  private fun InputStream.toJsonObject():JsonObject = readFully().toJsonObject()
  private fun InputStream.readFully():String {
    try {
      return readToByteBuffer(this.available()).array().contentToString()
//
    } finally {
      close()
    }
  }
}
