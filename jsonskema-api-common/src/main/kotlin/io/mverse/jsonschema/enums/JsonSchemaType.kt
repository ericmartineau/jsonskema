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

    override fun deserialize(decoder: Decoder): JsonSchemaType {
      return JsonSchemaTypes.fromString(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, obj: JsonSchemaType) {
      encoder.encodeString(obj.name.toLowerCase())
    }
  }
}
