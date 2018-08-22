package io.mverse.jsonschema.enums;

import io.mverse.jsonschema.SchemaException
import kotlinx.serialization.KInput
import kotlinx.serialization.KOutput
import kotlinx.serialization.Serializer
import kotlinx.serialization.Transient
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


@Serializer(forClass = JsonSchemaType::class)
class JsonSchemaTypeSerializer {

  override fun save(output: KOutput, obj: JsonSchemaType) {
    output.writeStringValue(obj.name.toLowerCase())
  }

  override fun load(input: KInput): JsonSchemaType {
    return JsonSchemaTypes.fromString(input.readStringValue())
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
