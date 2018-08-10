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
import io.mverse.jsonschema.loading.LoadingReport
import io.mverse.jsonschema.loading.SchemaLoader
import io.mverse.jsonschema.utils.Schemas.emptySchema
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

  protected constructor(location: SchemaLocation, refURI: URI, refSchema: Schema) : super(location, refURI, refSchema)

  override fun asDraft6(): Draft6Schema {
    val schema = refSchema ?: emptySchema
    return Draft6RefSchemaImpl(location, refURI, schema.asDraft6())
  }

  override fun asDraft7(): Draft7Schema {
    val schema = refSchema ?: emptySchema
    return Draft7RefSchemaImpl(location, refURI, schema.asDraft7())
  }

  override fun asDraft4(): Draft4Schema {
    val schema = refSchema ?: emptySchema
    return Draft4RefSchemaImpl(location, refURI, schema.asDraft4())
  }

  override fun asDraft3(): Draft3Schema {
    val schema = refSchema ?: emptySchema
    return Draft3RefSchemaImpl(location, refURI, schema.asDraft3())
  }

  override fun withId(id: URI): Schema {
    return RefSchemaImpl(location.withId(id), refURI, this)
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

  class RefSchemaBuilder(var refURI:URI? = null)

  companion object {
    fun refSchemaBuilder(refURI: URI): RefSchemaBuilder {
      return RefSchemaBuilder(refURI = refURI)
    }
  }
}
