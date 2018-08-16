package io.mverse.jsonschema.impl

import io.mverse.jsonschema.Draft3Schema
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaLocation
import io.mverse.jsonschema.enums.JsonSchemaVersion
import lang.URI

class Draft3RefSchemaImpl(location: SchemaLocation,
                          refURI: URI,
                          private val draft3: Draft3Schema)
  : RefSchemaImpl(location, refURI, draft3), Draft3Schema by draft3 {

  override val version: JsonSchemaVersion = JsonSchemaVersion.Draft3

  override fun withId(id: URI): Schema {
    return Draft3RefSchemaImpl(location.withId(id), refURI, this)
  }

  override fun asDraft3(): Draft3Schema = draft3
}
