package io.mverse.jsonschema.enums

import io.mverse.jsonschema.SchemaException
import kotlinx.serialization.json.ElementType

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

  val appliesTo: ElementType by lazy {
    when (this) {
      INTEGER -> ElementType.NUMBER
      BOOLEAN -> ElementType.BOOLEAN
      STRING -> ElementType.STRING
      NUMBER -> ElementType.NUMBER
      NULL -> ElementType.NULL
      OBJECT -> ElementType.OBJECT
      ARRAY -> ElementType.ARRAY
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
