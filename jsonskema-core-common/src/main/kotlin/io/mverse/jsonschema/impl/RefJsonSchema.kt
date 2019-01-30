package io.mverse.jsonschema.impl

import io.mverse.jsonschema.AllKeywords
import io.mverse.jsonschema.Draft3Schema
import io.mverse.jsonschema.Draft4Schema
import io.mverse.jsonschema.Draft6Schema
import io.mverse.jsonschema.Draft7Schema
import io.mverse.jsonschema.MergeReport
import io.mverse.jsonschema.RefSchema
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaLocation
import io.mverse.jsonschema.builder.MutableJsonSchema
import io.mverse.jsonschema.builder.MutableSchema
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.enums.JsonSchemaVersion.Draft3
import io.mverse.jsonschema.enums.JsonSchemaVersion.Draft4
import io.mverse.jsonschema.enums.JsonSchemaVersion.Draft6
import io.mverse.jsonschema.enums.JsonSchemaVersion.Draft7
import io.mverse.jsonschema.keyword.IdKeyword
import io.mverse.jsonschema.keyword.Keyword
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.Keywords.DOLLAR_ID
import io.mverse.jsonschema.keyword.Keywords.ID
import io.mverse.jsonschema.keyword.Keywords.REF
import io.mverse.jsonschema.keyword.URIKeyword
import io.mverse.jsonschema.loading.LoadingReport
import io.mverse.jsonschema.loading.SchemaLoader
import io.mverse.logging.mlogger
import lang.exception.illegalState
import lang.hashKode
import lang.json.JsonPath
import lang.json.JsrObject
import lang.json.jsrObject
import lang.json.writeJson
import lang.net.URI
import lang.suppress.Suppressions

data class RefJsonSchema(val schemaLoader: SchemaLoader,
                         override val refURI: URI,
                         private var internalParent: Schema?,
                         internal var internalLocation: SchemaLocation,
                         override val version: JsonSchemaVersion,
                         private var internalResolved: Schema? = null,
                         val refResolver: (RefSchema) -> Schema? = { internalResolved }
) : RefSchema, AllKeywords {

  constructor(schemaLoader: SchemaLoader,
              location: SchemaLocation,
              refURI: URI,
              version: JsonSchemaVersion,
              currentDocument: JsrObject,
              report: LoadingReport) : this(schemaLoader, refURI, null, location, version,
      refResolver = { refSchema ->
        refSchema.resolve(schemaLoader, currentDocument, report)
      })

  constructor(schemaLoader: SchemaLoader, location: SchemaLocation, refURI: URI, refSchema: Schema) : this(schemaLoader, refURI, null, location, refSchema.version, refSchema)
  constructor(schemaLoader: SchemaLoader, location: SchemaLocation, refURI: URI) : this(schemaLoader, refURI, null, location, Draft7, null)

  init {
    JsonSchema.initialize()
  }

  override val id: URI? by lazy {
    val kw = keywords[DOLLAR_ID] ?: keywords[ID]
    (kw as? IdKeyword)?.value
  }

  override val location: SchemaLocation get() = internalLocation

  override val refSchemaOrNull: Schema? get() = getOrResolveSchema()

  /**
   * Reference to the loaded schema
   */
  override val refSchema: Schema
    get() = getOrResolveSchema() ?: illegalState("Ref schema couldn't be resolved: $refURI")

  override val extraProperties get() = refSchema.extraProperties
  override val keywords: Map<KeywordInfo<*>, Keyword<*>>
    get() {
      return refSchema.keywords
    }

  @Suppress(Suppressions.NAME_SHADOWING)
  override fun merge(path: JsonPath, override: Schema?, report: MergeReport, mergedId: URI?): Schema {
    return override ?: this
  }

  override fun toJson(includeExtraProperties: Boolean): JsrObject {
    return jsrObject {
      REF.key *= refURI
    }
  }

  override fun withId(id: URI): Schema {
    return RefJsonSchema(schemaLoader, location.withId(id), refURI, refSchema)
  }

  override fun withDocumentURI(documentURI: URI): Schema {
    return RefJsonSchema(schemaLoader, location.withDocumentURI(documentURI), refURI, refSchema)
  }

  override fun toMutableSchema(): MutableSchema {
    @Suppress("unchecked_cast")
    return MutableJsonSchema(schemaLoader, fromSchema = this)
  }

  override fun toMutableSchema(id: URI): MutableSchema {
    @Suppress("unchecked_cast")
    return MutableJsonSchema(schemaLoader, fromSchema = this, id = id)
  }

  private fun getOrResolveSchema(): Schema? {
    if (internalResolved == null) {
      when (val resolved = refResolver(this)) {
        is RefSchema -> internalResolved = resolved.refSchemaOrNull
        is Schema -> internalResolved = resolved
      }
    }
    return internalResolved
  }

  /**
   * Ref schemas don't support navigating to subschemas
   */
  override fun getOrNull(path: JsonPath): Schema? {
    return null
  }

  override fun draft3(): Draft3Schema = copy(version = Draft3)
  override fun draft4(): Draft4Schema = copy(version = Draft4)
  override fun draft6(): Draft6Schema = copy(version = Draft6)
  override fun draft7(): Draft7Schema = copy(version = Draft7)

  override fun equals(other: Any?): Boolean {
    return when (other) {
      is RefSchema -> other.refURI == this.refURI
      is Schema -> (other.keywords[REF] as? URIKeyword)?.value == this.refURI
      else -> false
    }
  }

  override fun hashCode(): Int {
    return hashKode(mapOf(REF to refURI))
  }

  override fun toString(): String {
    return toString(includeExtraProperties = true, indent = true)
  }

  override fun toString(includeExtraProperties: Boolean, indent: Boolean): String {
    return toJson(includeExtraProperties).writeJson(indent)
  }

  companion object {
    val log = mlogger {}

    fun RefSchema.resolve(loader: SchemaLoader, doc: JsrObject?, report: LoadingReport): Schema? {
      return loader.loadRefSchema(this, this.refURI, doc, report)
    }
  }
}
