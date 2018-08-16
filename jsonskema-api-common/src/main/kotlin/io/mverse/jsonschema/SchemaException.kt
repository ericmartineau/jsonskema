package io.mverse.jsonschema

import io.mverse.jsonschema.utils.JsonUtils
import lang.URI
import lang.format

open class SchemaException : RuntimeException {
  val schemaLocation: String?

  constructor(schemaLocation: URI, message: String) : this(schemaLocation.toString(), message) {}

  constructor(schemaLocation: URI, message: String, vararg params: Any) : this(schemaLocation.toString(),
      message.format(*JsonUtils.prettyPrintArgs(*params)))

  constructor(schemaLocation: String?, message: String) : super(if (schemaLocation == null)
    "<unknown location>: $message"
  else
    "$schemaLocation: $message") {
    this.schemaLocation = schemaLocation
  }

  constructor(message: String) : this(null as String?, message) {}

  companion object {

    private val serialVersionUID = 5987489689035036987L
  }
}
