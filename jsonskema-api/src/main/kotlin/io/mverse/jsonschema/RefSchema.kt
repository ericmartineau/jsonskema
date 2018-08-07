package io.mverse.jsonschema

import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.JsonSchemaKeyword
import io.mverse.jsonschema.loading.LoadingReport
import io.mverse.jsonschema.loading.SchemaLoader
import kotlinx.serialization.json.JsonBuilder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import lang.URI
import lang.hashKode

/**
 * This class is used to resolve JSON pointers. during the construction of the schema. This class has been made mutable
 * to permit the loading of recursive schemas.
 */
abstract class RefSchema : Schema {

  /**
   * Contains a reference to the actual loaded schema.
   */
  val refSchema: Schema?

  override val location: SchemaLocation

  val refURI: URI

  override val id: URI?
    get() = refSchema!!.id

  override val schemaURI: URI?
    get() = refSchema!!.schemaURI

  override val title: String?
    get() = refSchema!!.title

  override val description: String?
    get() = refSchema!!.description

  override val extraProperties: Map<String, JsonElement>
    get() = refSchema!!.extraProperties

  override val keywords: Map<KeywordInfo<*>, JsonSchemaKeyword<*>>
    get() = requireRefSchema()!!.keywords

  constructor(factory: SchemaLoader?,
              location: SchemaLocation,
              refURI: URI,
              currentDocument: JsonObject,
              report: LoadingReport) {
    this.location = location
    this.refURI = refURI

    var infiniteLoopPrevention = 0
    if (factory != null) {
      var schema: Schema = this
      var thisRefURI = this.refURI

      while (schema is RefSchema) {
        schema = factory.loadRefSchema(schema, thisRefURI, currentDocument, report)
        if (schema is RefSchema) {
          thisRefURI = (schema as RefSchema).refURI
        }
        if (infiniteLoopPrevention++ > 10) {
          throw IllegalStateException("Too many nested references")
        }
      }
      this.refSchema = schema
    } else {
      this.refSchema = null
    }
  }

  protected constructor(location: SchemaLocation, refURI: URI, refSchema: Schema) {
    this.location = location
    this.refURI = refURI
    this.refSchema = refSchema
  }

  fun requireRefSchema(): Schema? {
    return refSchema
  }

  override fun toString(): String {
    return toJson(version ?: JsonSchemaVersion.latest()).toString()
  }

  abstract override fun toJson(version: JsonSchemaVersion): JsonObject
  abstract override fun asDraft6(): Draft6Schema
  abstract override fun asDraft3(): Draft3Schema
  abstract override fun asDraft4(): Draft4Schema

  override fun equals(other: Any?): Boolean {
    return other is RefSchema && other.refURI == this.refURI
  }

  override fun hashCode(): Int {
    return hashKode(refURI)
  }
}
