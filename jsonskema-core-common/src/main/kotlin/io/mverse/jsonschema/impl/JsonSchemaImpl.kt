package io.mverse.jsonschema.impl

import io.mverse.jsonschema.DraftSchema
import io.mverse.jsonschema.KeywordContainer
import io.mverse.jsonschema.MergeReport
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaLocation
import io.mverse.jsonschema.builder.MutableJsonSchema
import io.mverse.jsonschema.builder.MutableSchema
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.keyword.IdKeyword
import io.mverse.jsonschema.keyword.Keyword
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.Keywords.DOLLAR_ID
import io.mverse.jsonschema.keyword.Keywords.ID
import io.mverse.jsonschema.loading.SchemaLoader
import io.mverse.logging.mlogger
import lang.collection.freezeMap
import lang.json.JsonPath
import lang.json.JsrObject
import lang.json.JsrValue
import lang.json.jsrJson
import lang.net.URI

internal data class JsonSchemaImpl(
    val schemaLoader: SchemaLoader,
    private var internalParent: Schema?,
    internal var internalLocation: SchemaLocation,
    override val keywords: Map<KeywordInfo<*>, Keyword<*>> = emptyMap(),
    override val extraProperties: Map<String, JsrValue> = mutableMapOf(),
    override val version: JsonSchemaVersion = JsonSchemaVersion.latest)
  : Schema, KeywordContainer({keywords.freezeMap()}) {

  protected constructor(schemaLoader: SchemaLoader, parent:Schema?, from: Schema, version: JsonSchemaVersion) :
      this(schemaLoader = schemaLoader,
          internalParent = parent,
          internalLocation = from.location,
          keywords = from.keywords,
          extraProperties = from.extraProperties,
          version = version)

  init {
    initialize()
  }

  override var parent: Schema?
    get() = internalParent
    set(value) {
      if (value != null) {
        internalParent = value
        relocate()
        _location = null
      }
    }

  private var _location:SchemaLocation? = null
  override val location: SchemaLocation
    get() = _location ?: calculateLocation()

  override val id: URI? by lazy {
    val kw = keywords[DOLLAR_ID] ?: keywords[ID]
    (kw as? IdKeyword)?.value
  }

  override fun toJson(version: JsonSchemaVersion): JsrObject {
    return asVersion(version).toJson(true)
  }

  override fun withVersion(version: JsonSchemaVersion): Schema {
    return this.copy(version = version)
  }

  override fun toString(): String = asDraft7().toString(true)

  override fun toMutableSchema(): MutableSchema {
    return MutableJsonSchema(schemaLoader, fromSchema = this)
  }

  override fun toMutableSchema(id: URI): MutableSchema {
    return MutableJsonSchema(schemaLoader, fromSchema = this, id = id)
  }

  override fun equals(other: Any?): Boolean {
    return when {
      this === other -> true
      other !is Schema && other !is DraftSchema -> false
      other is Schema-> other.keywords == keywords
      other is DraftSchema -> other.keywords == keywords
      else -> false
    }
  }

  override fun merge(path: JsonPath, override: Schema?, report: MergeReport, mergedId: URI?): Schema {
    val overrides = override ?: return this
    val source = if (mergedId == null) this else this.withDocumentURI(mergedId)
    val mutable = source.toMutableSchema()
    mutable.merge(path, overrides, report)
    val built = mutable.build()
    return built
    //    return when (true) {
    //      null-> toBuilder.build()
    //      else-> toBuilder.build(SchemaLocation.documentRoot(mergedId), LoadingReport())
    //    }
  }

  override fun withId(id: URI): Schema {
    return copy(
        internalLocation = location.withId(id),
        keywords = keywords.toMutableMap().also {
          if (version == JsonSchemaVersion.Draft3) {
            it.remove(DOLLAR_ID)
            it[ID] = IdKeyword(id)
          } else {
            it.remove(ID)
            it[DOLLAR_ID] = IdKeyword(id)
          }
        })
  }

  override fun withDocumentURI(documentURI: URI): Schema {
    val location = location.withDocumentURI(documentURI)
    return copy(
        internalLocation = location,
        keywords = keywords.toMutableMap().also {
          if (version == JsonSchemaVersion.Draft3) {
            it.remove(DOLLAR_ID)
            it[ID] = IdKeyword(documentURI)
          } else {
            it.remove(ID)
            it[DOLLAR_ID] = IdKeyword(documentURI)
          }
        })
  }

  override fun hashCode(): Int {
    return keywords.hashCode()
  }

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

  companion object {

    val log = mlogger {}

    /**
     * This function just ensures that the companion object is loaded and the init block is run.
     */
    fun initialize() {}

    init {
      jsrJson.registerConversion<Schema> {
        it.asDraft7().toJson(true)
      }
    }
  }
}

