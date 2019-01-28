package io.mverse.jsonschema

import lang.net.URI

/**
 * This class is used to resolve JSON pointers. during the construction of the schema. This class has been made mutable
 * to permit the loading of recursive schemas.
 */
interface RefSchema: Schema {
  val refSchemaOrNull: Schema?
  val refSchema: Schema
  val refURI: URI
}
