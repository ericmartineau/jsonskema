package io.mverse.jsonschema

import lang.net.URI

open class SchemaException : RuntimeException {
  val schemaLocation: String?

  constructor(schemaLocation: URI, message: String) : this(schemaLocation.toString(), message) {}

  constructor(schemaLocation: String?, message: String) : super(if (schemaLocation == null)
    "<unknown location>: $message"
  else
    "$schemaLocation: $message") {
    this.schemaLocation = schemaLocation
  }

  constructor(message: String) : this(null as String?, message) {}
}

fun schemaException(location: URI, message: String): Nothing = throw SchemaException(location, message)
fun schemaException(location: String?, message: String): Nothing = throw SchemaException(location, message)
