package io.mverse.jsonschema.enums

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.SerialClassDescImpl

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

  @Serializer(forClass = JsonSchemaType::class)
  companion object : KSerializer<JsonSchemaType> {
    override val descriptor: SerialDescriptor = SerialClassDescImpl("JsonSchemaType")

    override fun deserialize(input: Decoder): JsonSchemaType {
      return JsonSchemaTypes.fromString(input.decodeString())
    }

    override fun serialize(output: Encoder, obj: JsonSchemaType) {
      output.encodeString(obj.name.toLowerCase())
    }
  }
}
