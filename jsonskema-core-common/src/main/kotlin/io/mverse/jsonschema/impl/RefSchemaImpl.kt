package io.mverse.jsonschema.impl

import io.mverse.jsonschema.DraftSchema
import io.mverse.jsonschema.JsonSchema.schemaLoader
import io.mverse.jsonschema.MergeReport
import io.mverse.jsonschema.RefSchema
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaLocation
import io.mverse.jsonschema.builder.MutableJsonSchema
import io.mverse.jsonschema.builder.MutableSchema
import io.mverse.jsonschema.enums.JsonSchemaVersion
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

data class RefSchemaImpl(val loader: SchemaLoader,
                         override val refURI: URI,
                         private var internalParent: Schema?,
                         internal var internalLocation: SchemaLocation,
                         override val version: JsonSchemaVersion,
                         private var internalResolved: Schema? = null,
                         val refResolver: (RefSchema) -> Schema? = { internalResolved }
) : RefSchema  {

  constructor(loader: SchemaLoader,
              location: SchemaLocation,
              refURI: URI,
              version: JsonSchemaVersion,
              currentDocument: JsrObject,
              report: LoadingReport) : this(loader, refURI, null, location, version,
      refResolver = { refSchema ->
        refSchema.resolve(loader, currentDocument, report)
      })

  constructor(factory: SchemaLoader, location: SchemaLocation, refURI: URI, refSchema: Schema) : this(factory, refURI, null, location, refSchema.version, refSchema)
  constructor(factory: SchemaLoader, location: SchemaLocation, refURI: URI) : this(factory, refURI,  null, location, Draft7, null)

  init {
    JsonSchemaImpl.initialize()
  }
  override val id: URI? by lazy {
    val kw = keywords[DOLLAR_ID] ?: keywords[ID]
    (kw as? IdKeyword)?.value
  }

  override var parent: Schema?
    get() = internalParent
    set(value) {
      if (value != null) {
        internalParent = value
        _location = null
      }
    }

  private var _location:SchemaLocation? = null
  override val location: SchemaLocation
    get() = _location ?: calculateLocation()

  private fun calculateLocation():SchemaLocation {
    val parent = parent
    _location = when (parent) {
      null-> internalLocation
      else-> {
        parent.location.withJsonPath(internalLocation.jsonPath)
      }
    }
    return _location!!
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

  override val refSchemaOrNull: Schema? get() = getOrResolveSchema()

  /**
   * Reference to the loaded schema
   */
  override val refSchema: Schema
    get() = getOrResolveSchema() ?: illegalState("Ref schema hasn't been resolved yet")

  override val extraProperties get() = refSchema.extraProperties
  override val keywords:Map<KeywordInfo<*>, Keyword<*>> get() {
    return refSchema.keywords
  }

  @Suppress(Suppressions.NAME_SHADOWING)
  override fun merge(path: JsonPath, override: Schema?, report: MergeReport, mergedId: URI?): Schema {
    return override ?: this
  }

  override fun equals(other: Any?): Boolean {
    return when (other) {
      is RefSchema -> other.refURI == this.refURI
      is DraftSchema -> (other.keywords[REF] as? URIKeyword)?.value == this.refURI
      else -> false
    }
  }

  override fun toJson(version: JsonSchemaVersion): JsrObject {
    return jsrObject {
      REF.key *= refURI
    }
  }

  override fun hashCode(): Int {
    return hashKode(mapOf(REF to refURI))
  }

  //  override fun asDraft6(): Draft6Schema {
  //    val draft6 = refSchemaOrNull?.asDraft6()
  //        ?: Draft6SchemaImpl(
  //            schemaLoader,
  //            location = location,
  //            keywords = mapOf(Keywords.REF to URIKeyword(refURI)))
  //    return Draft6RefSchemaImpl(schemaLoader, location, refURI, draft6)
  //  }
  //
  //  override fun asDraft7(): Draft7Schema {
  //    val draft7 = refSchemaOrNull?.asDraft7()
  //        ?: Draft7SchemaImpl(schemaLoader,
  //            location = location,
  //            keywords = mapOf(Keywords.REF to URIKeyword(refURI)),
  //            extraProperties = emptyMap())
  //    return Draft7RefSchemaImpl(schemaLoader, location, refURI, draft7)
  //  }
  //
  //  override fun asDraft4(): Draft4Schema {
  //    val draft4 = refSchemaOrNull?.asDraft4()
  //        ?: Draft4SchemaImpl(schemaLoader,
  //            location = location,
  //            keywords = mapOf(Keywords.REF to URIKeyword(refURI)),
  //            extraProperties = emptyMap())
  //    return Draft4RefSchemaImpl(schemaLoader, location, refURI, draft4)
  //  }
  //
  //  override fun asDraft3(): Draft3Schema {
  //    val draft3 = refSchemaOrNull?.asDraft3()
  //        ?: Draft3SchemaImpl(schemaLoader,
  //            location = location,
  //            keywords = mapOf(Keywords.REF to URIKeyword(refURI)),
  //            extraProperties = emptyMap())
  //    return Draft3RefSchemaImpl(schemaLoader, location, refURI, draft3)
  //  }

  override fun withId(id: URI): Schema {
    return RefSchemaImpl(schemaLoader, location.withId(id), refURI, refSchema)
  }

  override fun withDocumentURI(documentURI: URI): Schema {
    return RefSchemaImpl(schemaLoader, location.withDocumentURI(documentURI), refURI, refSchema)
  }

  override fun toMutableSchema(): MutableSchema {
    @Suppress("unchecked_cast")
    return MutableJsonSchema(schemaLoader, fromSchema = this)
  }

  override fun toMutableSchema(id: URI): MutableSchema {
    @Suppress("unchecked_cast")
    return MutableJsonSchema(schemaLoader, fromSchema = this, id = id)
  }

  fun toJson(): JsrObject {
    return jsrObject {
      "\$ref" *= refURI.toString()
    }
  }

  fun toString(indent: Boolean): String =
      if (indent) toJson().writeJson(indent) else toString()

  override fun toString(): String {
    return toString(indent = true)
  }

  override fun withVersion(version: JsonSchemaVersion): Schema {
    return copy(version = version)
  }

  companion object {
    val log = mlogger {}

    fun RefSchema.resolve(loader: SchemaLoader, doc: JsrObject?, report: LoadingReport): Schema? {
      return loader.loadRefSchema(this, this.refURI, doc, report)
    }
  }
}
