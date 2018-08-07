package io.mverse.jsonschema.enums

import io.mverse.jsonschema.SchemaException
import lang.json.ValueType

/**
 * Represents the valid json-schema types.
 */
enum class JsonSchemaType {
  STRING,
  BOOLEAN,
  NUMBER,
  INTEGER,
  NULL,
  OBJECT,
  ARRAY;

  override fun toString(): String {
    return name.toLowerCase()
  }

  val appliesTo: ValueType by lazy {
    when (this) {
      INTEGER -> ValueType.NUMBER
      BOOLEAN -> ValueType.BOOLEAN
      STRING -> ValueType.STRING
      NUMBER -> ValueType.NUMBER
      NULL -> ValueType.NULL
      OBJECT -> ValueType.OBJECT
      ARRAY -> ValueType.ARRAY
    }
  }

  companion object {
    fun fromString(type: String?): JsonSchemaType {
      if (type == null) {
        return NULL
      }
      try {
        return valueOf(type.toUpperCase())
      } catch (e: IllegalArgumentException) {
        throw SchemaException("Invalid schema type:$type")
      }
    }
  }
}
