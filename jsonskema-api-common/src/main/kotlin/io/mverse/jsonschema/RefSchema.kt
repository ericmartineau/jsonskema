package io.mverse.jsonschema

import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.loading.LoadingReport
import io.mverse.jsonschema.loading.SchemaLoader
import lang.exception.illegalState
import lang.hashKode
import lang.json.JsonPath
import lang.json.JsrObject
import lang.net.URI

/**
 * This class is used to resolve JSON pointers. during the construction of the schema. This class has been made mutable
 * to permit the loading of recursive schemas.
 */
abstract class RefSchema(
    /**
     * The location this schema is referenced from
     */
    override val location: SchemaLocation,

    /**
     * The location of this ref schema
     */
    val refURI: URI,

    /**
     * This class can be used as either a resolved schema, or as a reference.  This callback function
     * allows for lazy loading the schema, while still leveraging the "this" value
     */
    refSchemaLoader: ((RefSchema) -> Schema?)?) : Schema {

  /**
   * Contains a reference to the actual loaded schema, or null if it hasn't been resolved
   */
  val refSchemaOrNull by lazy { refSchemaLoader?.invoke(this) }

  /**
   * Reference to the loaded schema
   */
  val refSchema: Schema get() = refSchemaOrNull ?: illegalState("Ref schema hasn't been resolved")

  /**
   * A non-resolved schema that contains just the ref keyword
   */
  abstract val refOnlySchema: Schema

  override val id: URI? get() = refSchema.id
  override val schemaURI: URI? get() = refSchema.schemaURI
  override val title: String? get() = refSchema.title
  override val description: String? get() = refSchema.description

  override val extraProperties get() = refSchema.extraProperties

  override val keywords get() = refSchema.keywords

  constructor(factory: SchemaLoader?,
              location: SchemaLocation,
              refURI: URI,
              currentDocument: JsrObject?,
              report: LoadingReport) : this(location, refURI, loader@{ thisSchema ->
    var infiniteLoopPrevention = 0

    when (factory) {
      null -> return@loader null
      else -> {
        var schema: Schema = thisSchema
        var thisRefURI = refURI
        while (schema is RefSchema) {
          schema = factory.loadRefSchema(schema, thisRefURI, currentDocument, report)
          if (schema is RefSchema) {
            thisRefURI = schema.refURI
          }
          if (infiniteLoopPrevention++ > 10) {
            throw IllegalStateException("Too many nested references")
          }
        }
        return@loader schema
      }
    }
  }) {
    // Force resolution of the schema
    this.refSchemaOrNull != null
  }

  protected constructor(location: SchemaLocation, refURI: URI, refSchema: Schema) :
      this(location, refURI, { refSchema })

  override fun toString(): String {
    return toJson(version ?: JsonSchemaVersion.latest).toString()
  }

  override fun merge(path: JsonPath, override: Schema, report: MergeReport): Schema = refSchema.merge(path, override, report)

  abstract override fun toJson(version: JsonSchemaVersion, includeExtraProperties: Boolean): JsrObject
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
