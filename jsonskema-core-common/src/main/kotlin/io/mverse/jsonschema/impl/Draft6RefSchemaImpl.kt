package io.mverse.jsonschema.impl

import io.mverse.jsonschema.Draft6Schema
import io.mverse.jsonschema.SchemaLocation
import io.mverse.jsonschema.enums.JsonSchemaVersion
import lang.net.URI

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
internal class Draft6RefSchemaImpl(location: SchemaLocation,
                                   refURI: URI,
                                   private val draft6: Draft6Schema)
  : RefSchemaImpl(location, refURI, draft6), Draft6Schema by draft6 {

  override val version: JsonSchemaVersion = JsonSchemaVersion.Draft6
  override fun asDraft6(): Draft6Schema = this
}
