package io.mverse.jsonschema.impl

import io.mverse.jsonschema.Draft3Schema
import io.mverse.jsonschema.Draft4Schema
import io.mverse.jsonschema.Draft6Schema
import io.mverse.jsonschema.Draft7Schema
import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.RefSchema
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaBuilder
import io.mverse.jsonschema.SchemaLocation
import io.mverse.jsonschema.builder.JsonSchemaBuilder
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.enums.JsonSchemaVersion.Draft7
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.URIKeyword
import io.mverse.jsonschema.loading.LoadingReport
import io.mverse.jsonschema.loading.SchemaLoader
import io.mverse.jsonschema.schema
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.json
import lang.URI

open class RefSchemaImpl : RefSchema {

  override val version: JsonSchemaVersion = Draft7

  constructor(factory: SchemaLoader?,
              location: SchemaLocation,
              refURI: URI,
              currentDocument: JsonObject?,
              report: LoadingReport) : super(factory, location, refURI, currentDocument, report)

  constructor(location: SchemaLocation, refURI: URI, refSchema: Schema) : super(location, refURI, refSchema)

  internal constructor(location: SchemaLocation, refURI: URI, refSchemaLoader: (Schema) -> Schema?) : super(location, refURI, refSchemaLoader)

  override fun asDraft6(): Draft6Schema {
    val draft6 = refSchemaOrNull?.asDraft6()
        ?: Draft6SchemaImpl(
            location = location,
            keywords = mapOf(Keywords.REF to URIKeyword(refURI)))
    return Draft6RefSchemaImpl(location, refURI, draft6)
  }

  override fun asDraft7(): Draft7Schema {
    val draft7 = refSchemaOrNull?.asDraft7()
        ?: Draft7SchemaImpl(
            location = location,
            keywords = mapOf(Keywords.REF to URIKeyword(refURI)),
            extraProperties = emptyMap())
    return Draft7RefSchemaImpl(location, refURI, draft7)
  }

  override fun asDraft4(): Draft4Schema {
    val draft4 = refSchemaOrNull?.asDraft4()
        ?: Draft4SchemaImpl(
            location = location,
            keywords = mapOf(Keywords.REF to URIKeyword(refURI)),
            extraProperties = emptyMap())
    return Draft4RefSchemaImpl(location, refURI, draft4)
  }

  override fun asDraft3(): Draft3Schema {
    val draft3 = refSchemaOrNull?.asDraft3()
        ?: Draft3SchemaImpl(
            location = location,
            keywords = mapOf(Keywords.REF to URIKeyword(refURI)),
            extraProperties = emptyMap())
    return Draft3RefSchemaImpl(location, refURI, draft3)
  }

  override fun withId(id: URI): Schema {
    return RefSchemaImpl(location.withId(id), refURI, this)
  }

  override val refOnlySchema: Schema
    get() {
      val thisRefURI = this.refURI
      return JsonSchema.schema {
        refURI = thisRefURI
      }
    }

  override fun toBuilder(): SchemaBuilder {
    @Suppress("unchecked_cast")
    return JsonSchemaBuilder(fromSchema = this)
  }

  override fun toBuilder(id: URI): SchemaBuilder {
    @Suppress("unchecked_cast")
    return JsonSchemaBuilder(fromSchema = this, id = id)
  }

  override fun toJson(version: JsonSchemaVersion): JsonObject {
    return json {
      "\$ref" to refURI.toString()
    }
  }

  override fun toString(version: JsonSchemaVersion): String = toString()
}
