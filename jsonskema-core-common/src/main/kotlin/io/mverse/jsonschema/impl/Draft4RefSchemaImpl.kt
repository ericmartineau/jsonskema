package io.mverse.jsonschema.impl

import io.mverse.jsonschema.Draft4Schema
import io.mverse.jsonschema.SchemaLocation
import io.mverse.jsonschema.enums.JsonSchemaVersion
import lang.URI

internal class Draft4RefSchemaImpl(location: SchemaLocation, refURI: URI, private val draft4: Draft4Schema)
  : RefSchemaImpl(location, refURI, draft4), Draft4Schema by draft4 {

  override val version: JsonSchemaVersion = JsonSchemaVersion.Draft4

  override fun asDraft4(): Draft4Schema = draft4
}
