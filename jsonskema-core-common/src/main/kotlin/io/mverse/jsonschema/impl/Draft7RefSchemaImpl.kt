package io.mverse.jsonschema.impl

import io.mverse.jsonschema.Draft7Schema
import io.mverse.jsonschema.SchemaLocation
import io.mverse.jsonschema.enums.JsonSchemaVersion
import lang.URI

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
internal class Draft7RefSchemaImpl(location: SchemaLocation,
                                   refURI: URI,
                                   private val draft7: Draft7Schema)
  : RefSchemaImpl(location, refURI, draft7), Draft7Schema by draft7 {

  override val version: JsonSchemaVersion = JsonSchemaVersion.Draft7
  override fun asDraft7(): Draft7Schema = this
}
