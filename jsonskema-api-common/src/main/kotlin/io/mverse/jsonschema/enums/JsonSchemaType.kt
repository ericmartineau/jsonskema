package io.mverse.jsonschema.enums

import io.mverse.jsonschema.SchemaException
import kotlinx.serialization.KInput
import kotlinx.serialization.KOutput
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.json.ElementType
import lang.Global

/**
 * Represents the valid json-schema types.
 */
@Serializable(with = JsonSchemaTypes::class)
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
}

@Serializer(forClass = JsonSchemaType::class)
object JsonSchemaTypes {

  override fun save(output: KOutput, obj: JsonSchemaType) {
    output.writeStringValue(obj.toString())
  }

  override fun load(input: KInput): JsonSchemaType {
    return fromString(input.readStringValue())
  }

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
