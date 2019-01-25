package io.mverse.jsonschema

import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.loading.LoadingReport
import io.mverse.jsonschema.loading.SchemaLoader
import io.mverse.logging.mlogger
import lang.exception.illegalState
import lang.exception.nullPointer
import lang.hashKode
import lang.json.JsonPath
import lang.json.JsrObject
import lang.net.URI
import lang.suppress.Suppressions.Companion.NAME_SHADOWING

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
     * Contains a reference to the actual loaded schema, or null if it hasn't been resolved.
     */
    refSchema: Schema? = null,
    val factory:SchemaLoader? = null) : Schema {

  constructor(factory: SchemaLoader,
              location: SchemaLocation,
              refURI: URI,
              currentDocument: JsrObject?,
              report: LoadingReport) : this(location, refURI, factory = factory) {
    asyncLoad(factory, currentDocument, report)
  }

  private fun asyncLoad(loader: SchemaLoader, doc: JsrObject?, report: LoadingReport) {
    val found = loader.loadRefSchema(this, refURI, doc, report) {
      if (it is RefSchema) {
        if (loadCounter++ > 10) illegalState("Stack overflow while loading schema $refURI")
        asyncLoad(loader, doc, report)
      } else {
        internalRefSchema = it
      }
    }
    if (found != null) {
      internalRefSchema = found
    }
  }

  private var loadCounter = 0
  private var internalRefSchema: Schema? = refSchema
    set(value) {
      field = value ?: nullPointer("The internal refSchema should never be set to null")
    }

  val refSchemaOrNull: Schema? get() = internalRefSchema

  /**
   * Reference to the loaded schema
   */
  val refSchema: Schema
    get() = refSchemaOrNull ?: factory?.findLoadedSchema(refURI) ?: illegalState("Ref schema hasn't been resolved yet")

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

  override fun toString(): String {
    return toJson(version ?: JsonSchemaVersion.latest).toString()
  }

  @Suppress(NAME_SHADOWING)
  override fun merge(path: JsonPath, override: Schema?, report: MergeReport): Schema {
    return override ?: this
  }

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

  companion object {
    val log = mlogger {}
  }
}
