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
import io.mverse.jsonschema.keyword
import io.mverse.jsonschema.keyword.IdKeyword
import io.mverse.jsonschema.keyword.Keyword
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.Keywords.DOLLAR_ID
import io.mverse.jsonschema.keyword.Keywords.ID
import io.mverse.jsonschema.loading.SchemaLoader
import io.mverse.logging.mlogger
import lang.exception.illegalState
import lang.json.JsonPath
import lang.json.JsrObject
import lang.json.JsrValue
import lang.json.jsrJson
import lang.json.jsrObject
import lang.json.writeJson
import lang.net.URI

data class JsonSchema(
    val schemaLoader: SchemaLoader,
    internal var internalLocation: SchemaLocation,
    override val keywords: Map<KeywordInfo<*>, Keyword<*>> = emptyMap(),
    override val extraProperties: Map<String, JsrValue> = mutableMapOf(),
    override val version: JsonSchemaVersion) : Schema, AllKeywords {

  init {
    initialize()
  }

  override val location: SchemaLocation
    get() = internalLocation

  override val id: URI? by lazy {
    val kw = keywords[DOLLAR_ID] ?: keywords[ID]
    (kw as? IdKeyword)?.value
  }

  override fun toMutableSchema(): MutableSchema {
    return MutableJsonSchema(schemaLoader, fromSchema = this)
  }

  override fun toMutableSchema(id: URI): MutableSchema {
    return MutableJsonSchema(schemaLoader, fromSchema = this, id = id)
  }

  override fun merge(path: JsonPath, override: Schema?, report: MergeReport, mergedId: URI?): Schema {
    val overrides = override ?: return this
    val source = if (mergedId == null) this else this.withDocumentURI(mergedId)
    val mutable = source.toMutableSchema()
    mutable.merge(path, overrides, report)
    val built = mutable.build()
    return built
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

  override fun getOrNull(path: JsonPath): Schema? {
    var schema: Schema = draft7()
    val segments = path.iterator()
    while (segments.hasNext()) {
      val draft7 = schema.draft7()
      val segment = segments.next()
      when (segment) {
        "properties"-> schema = draft7.properties[segments.next()] ?: return null
        "definitions"-> schema = draft7.definitions[segments.next()] ?: return null
        "oneOf" -> schema = draft7.oneOfSchemas.getOrNull(segments.next().toInt()) ?: return null
        "anyOf" -> schema = draft7.anyOfSchemas.getOrNull(segments.next().toInt()) ?: return null
        "allOf" -> schema = draft7.allOfSchemas.getOrNull(segments.next().toInt()) ?: return null
        "if" -> schema = draft7.ifSchema ?: return null
        "else" -> schema = draft7.elseSchema ?: return null
        "then" -> schema = draft7.thenSchema ?: return null
        "not" -> schema = draft7.notSchema ?: return null
        "contains" -> schema = draft7.containsSchema ?: return null
        "propertyNames" -> schema = draft7.propertyNameSchema ?: return null
        "items" -> illegalState("Items not implemented")
        "dependencies" -> illegalState("Dependencies not implemented")
        else-> illegalState("$segment can't be resolved within document")
      }
    }
    return schema
  }

  override fun draft3(): Draft3Schema {
    return copy(version = JsonSchemaVersion.Draft3)
  }

  override fun draft4(): Draft4Schema {
    return copy(version = JsonSchemaVersion.Draft4)
  }

  override fun draft6(): Draft6Schema {
    return copy(version = JsonSchemaVersion.Draft6)
  }

  override fun draft7(): Draft7Schema {
    return copy(version = JsonSchemaVersion.Draft7)
  }

  override fun toString(): String = toString(includeExtraProperties = true, indent = true)

  override fun toString(includeExtraProperties: Boolean, indent: Boolean): String {
    val json = toJson(includeExtraProperties)
    return if(indent) json.writeJson(indent) else json.toString()
  }

  override fun toJson(includeExtraProperties: Boolean): JsrObject {
    return jsrObject {
      when {
        this is RefSchema -> Keywords.REF.key *= this.refURI
        Keywords.REF in keywords -> Keywords.REF.key *= keyword(Keywords.REF)?.value?.toString()!!
        else -> {
          forEachSortedKeyword { keyword, keywordValue ->
            if (keyword.applicableVersions.contains(version)) {
              keywordValue.toJson(keyword, this, version, includeExtraProperties)
            } else {
              log.warn { "Keyword ${keyword.key} does not apply to version: [$version], only for ${keyword.applicableVersions}" }
            }
          }
          if (includeExtraProperties) {
            extraProperties.forEach { (prop, value) ->
              prop *= value
            }
          }
        }
      }
    }
  }

  fun forEachSortedKeyword(block: (KeywordInfo<*>, Keyword<*>) -> Unit) {
    keywords.map { it.key to it.value }
        .sortedBy { it.first.sortOrder }
        .forEach { (first, second) ->
          block(first, second)
        }
  }


  override fun equals(other: Any?): Boolean {
    return when {
      this === other -> true
      other !is Schema -> false
      else -> other.keywords == keywords
    }
  }

  override fun hashCode(): Int {
    return keywords.hashCode()
  }

  companion object {
    val log = mlogger {}

    /**
     * This function just ensures that the companion object is loaded and the init block is run.
     */
    fun initialize() {}

    init {
      jsrJson.registerConversion<Schema> {
        it.draft7().toJson(true)
      }
    }
  }
}

