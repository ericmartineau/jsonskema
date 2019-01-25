package io.mverse.jsonschema.impl

import io.mverse.jsonschema.Draft7Schema
import io.mverse.jsonschema.SchemaLocation
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.loading.SchemaLoader
import lang.net.URI

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
internal class Draft7RefSchemaImpl(schemaLoader: SchemaLoader,
                                   location: SchemaLocation,
                                   refURI: URI,
                                   private val draft7: Draft7Schema)
  : RefSchemaImpl(schemaLoader, location, refURI, draft7), Draft7Schema by draft7 {

  override val location: SchemaLocation get() = super.location
  override val version: JsonSchemaVersion = JsonSchemaVersion.Draft7
  override fun asDraft7(): Draft7Schema = this
}
