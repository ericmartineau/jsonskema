package io.mverse.jsonschema.enums;

import io.mverse.jsonschema.SchemaException
import kotlinx.serialization.Transient
import lang.Global
import lang.collection.asList
import lang.json.JsrType

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
val JsonSchemaType.appliesTo: JsrType
  get() {
    return when (this) {
      JsonSchemaType.INTEGER -> JsrType.NUMBER
      JsonSchemaType.BOOLEAN -> JsrType.BOOLEAN
      JsonSchemaType.STRING -> JsrType.STRING
      JsonSchemaType.NUMBER -> JsrType.NUMBER
      JsonSchemaType.NULL -> JsrType.NULL
      JsonSchemaType.OBJECT -> JsrType.OBJECT
      JsonSchemaType.ARRAY -> JsrType.ARRAY
    }
  }
