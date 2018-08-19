package io.mverse.jsonschema.impl

import io.mverse.jsonschema.Draft3Schema
import io.mverse.jsonschema.Draft4Schema
import io.mverse.jsonschema.Draft6Schema
import io.mverse.jsonschema.Draft7Schema
import io.mverse.jsonschema.RefSchema
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaBuilder
import io.mverse.jsonschema.SchemaLocation
import io.mverse.jsonschema.builder.JsonSchemaBuilder
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.enums.JsonSchemaVersion.Draft7
import io.mverse.jsonschema.jsonschema
import io.mverse.jsonschema.loading.LoadingReport
import io.mverse.jsonschema.loading.SchemaLoader
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

  override fun asDraft6(): Draft6Schema {
    return Draft6RefSchemaImpl(location, refURI, (refSchemaOrNull ?: refOnlySchema).asDraft6())
  }

  override fun asDraft7(): Draft7Schema {
    return Draft7RefSchemaImpl(location, refURI, (refSchemaOrNull ?: refOnlySchema).asDraft7())
  }

  override fun asDraft4(): Draft4Schema {
    return Draft4RefSchemaImpl(location, refURI, (refSchemaOrNull ?: refOnlySchema).asDraft4())
  }

  override fun asDraft3(): Draft3Schema {
    return Draft3RefSchemaImpl(location, refURI, (refSchemaOrNull ?: refOnlySchema).asDraft3())
  }

  override fun withId(id: URI): Schema {
    return RefSchemaImpl(location.withId(id), refURI, this)
  }

  override val refOnlySchema: Schema
    get() {
      val thisRefURI = this.refURI
      return jsonschema {
        refURI = thisRefURI
      }
    }

  override fun <X : SchemaBuilder<X>> toBuilder(): X {
    @Suppress("unchecked_cast")
    return JsonSchemaBuilder(fromSchema = this) as X
  }

  override fun <X : SchemaBuilder<X>> toBuilder(id: URI): X {
    @Suppress("unchecked_cast")
    return JsonSchemaBuilder(fromSchema = this, id = id) as X
  }

  override fun toJson(version: JsonSchemaVersion): JsonObject {
    return json {
      "\$ref" to refURI.toString()
    }
  }

  override fun toString(version: JsonSchemaVersion): String = toString()
}
