package io.mverse.jsonschema.enums

import io.mverse.jsonschema.SchemaException
import io.mverse.jsonschema.enums.JsonSchemaTypes.fromString
import kotlinx.serialization.KInput
import kotlinx.serialization.KOutput
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.json.ElementType
import lang.Global

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

  companion object {
    fun serializer(): KSerializer<JsonSchemaType> = JsonSchemaTypeSerializer()
  }
}
