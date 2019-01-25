package io.mverse.jsonschema.impl

import io.mverse.jsonschema.Draft3Schema
import io.mverse.jsonschema.Draft4Schema
import io.mverse.jsonschema.Draft6Schema
import io.mverse.jsonschema.Draft7Schema
import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.RefSchema
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaLocation
import io.mverse.jsonschema.builder.MutableJsonSchema
import io.mverse.jsonschema.builder.MutableSchema
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.enums.JsonSchemaVersion.Draft7
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.URIKeyword
import io.mverse.jsonschema.loading.LoadingReport
import io.mverse.jsonschema.loading.SchemaLoader
import lang.json.JsrObject
import lang.json.jsrObject
import lang.json.writeJson
import lang.net.URI

open class RefSchemaImpl : RefSchema {

  override val version: JsonSchemaVersion = Draft7

  constructor(factory: SchemaLoader,
              location: SchemaLocation,
              refURI: URI,
              currentDocument: JsrObject?,
              report: LoadingReport) : super(factory, location, refURI, currentDocument, report)

  constructor(factory: SchemaLoader, location: SchemaLocation, refURI: URI, refSchema: Schema) : super(factory, location, refURI, refSchema)
  constructor(factory: SchemaLoader, location: SchemaLocation, refURI: URI) : super(factory, location, refURI)

  init {
    JsonSchemaImpl.initialize()
  }

  override fun asDraft6(): Draft6Schema {
    val draft6 = refSchemaOrNull?.asDraft6()
        ?: Draft6SchemaImpl(
            schemaLoader,
            location = location,
            keywords = mapOf(Keywords.REF to URIKeyword(refURI)))
    return Draft6RefSchemaImpl(schemaLoader, location, refURI, draft6)
  }

  override fun asDraft7(): Draft7Schema {
    val draft7 = refSchemaOrNull?.asDraft7()
        ?: Draft7SchemaImpl(schemaLoader,
            location = location,
            keywords = mapOf(Keywords.REF to URIKeyword(refURI)),
            extraProperties = emptyMap())
    return Draft7RefSchemaImpl(schemaLoader,location, refURI, draft7)
  }

  override fun asDraft4(): Draft4Schema {
    val draft4 = refSchemaOrNull?.asDraft4()
        ?: Draft4SchemaImpl(schemaLoader,
            location = location,
            keywords = mapOf(Keywords.REF to URIKeyword(refURI)),
            extraProperties = emptyMap())
    return Draft4RefSchemaImpl(schemaLoader,location, refURI, draft4)
  }

  override fun asDraft3(): Draft3Schema {
    val draft3 = refSchemaOrNull?.asDraft3()
        ?: Draft3SchemaImpl(schemaLoader,
            location = location,
            keywords = mapOf(Keywords.REF to URIKeyword(refURI)),
            extraProperties = emptyMap())
    return Draft3RefSchemaImpl(schemaLoader,location, refURI, draft3)
  }

  override fun withId(id: URI): Schema {
    return RefSchemaImpl(schemaLoader, location.withId(id), refURI, this)
  }

  override val refOnlySchema: Schema
    get() {
      val thisRefURI = this.refURI
      return JsonSchema.schema {
        refURI = thisRefURI
      }
    }

  override fun toMutableSchema(): MutableSchema {
    @Suppress("unchecked_cast")
    return MutableJsonSchema(schemaLoader, fromSchema = this)
  }

  override fun toMutableSchema(id: URI): MutableSchema {
    @Suppress("unchecked_cast")
    return MutableJsonSchema(schemaLoader, fromSchema = this, id = id)
  }

  override fun toJson(version: JsonSchemaVersion, includeExtraProperties: Boolean): JsrObject {
    return jsrObject {
      "\$ref" *= refURI.toString()
    }
  }

  override fun toString(version: JsonSchemaVersion, includeExtraProperties: Boolean, indent: Boolean): String =
      if (indent) toJson(version, includeExtraProperties).writeJson(indent) else toString()
}
