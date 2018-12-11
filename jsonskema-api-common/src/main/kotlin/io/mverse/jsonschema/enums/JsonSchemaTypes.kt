package io.mverse.jsonschema.enums;

import io.mverse.jsonschema.SchemaException
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KInput
import kotlinx.serialization.KOutput
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializer
import kotlinx.serialization.Transient
import kotlinx.serialization.internal.SerialClassDescImpl
import kotlinx.serialization.json.ElementType
import lang.Global

object JsonSchemaTypes {
  @Global
  fun fromString(type: String?): JsonSchemaType {
    if (type == null) {
      return JsonSchemaType.NULL
    }
    try {
      return JsonSchemaType.valueOf(type.toUpperCase())
    } catch (e: IllegalArgumentException) {
      throw SchemaException("Invalid schema type:$type")
    }
  }
}

@Transient
val JsonSchemaType.appliesTo: ElementType
  get() {
    return when (this) {
      JsonSchemaType.INTEGER -> ElementType.NUMBER
      JsonSchemaType.BOOLEAN -> ElementType.BOOLEAN
      JsonSchemaType.STRING -> ElementType.STRING
      JsonSchemaType.NUMBER -> ElementType.NUMBER
      JsonSchemaType.NULL -> ElementType.NULL
      JsonSchemaType.OBJECT -> ElementType.OBJECT
      JsonSchemaType.ARRAY -> ElementType.ARRAY
    }
  }
