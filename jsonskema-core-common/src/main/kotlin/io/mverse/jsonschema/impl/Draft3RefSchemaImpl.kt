package io.mverse.jsonschema.impl

import io.mverse.jsonschema.Draft3Schema
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaLocation
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.loading.SchemaLoader
import lang.net.URI

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class Draft3RefSchemaImpl(schemaLoader: SchemaLoader, location: SchemaLocation,
                          refURI: URI,
                          private val draft3: Draft3Schema)
  : RefSchemaImpl(schemaLoader, location, refURI, draft3), Draft3Schema by draft3 {

  override val location: SchemaLocation get() = super.location
  override val version: JsonSchemaVersion = JsonSchemaVersion.Draft3
  override fun withId(id: URI): Schema {
    return Draft3RefSchemaImpl(schemaLoader, location.withId(id), refURI, this)
  }

  override fun asDraft3(): Draft3Schema = draft3
}
