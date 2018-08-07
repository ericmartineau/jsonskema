package lang.json

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonLiteral
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import lang.illegalState

enum class ValueType {
  NUMBER,
  BOOLEAN,
  ARRAY,
  INTEGER,
  STRING,
  OBJECT,
  NULL
}

fun String?.toJson(): JsonPrimitive {
  return JsonPrimitive(this)
}

fun Number?.toJson(): JsonPrimitive {
  return JsonPrimitive(this)
}

fun Boolean?.toJson(): JsonPrimitive {
  return JsonPrimitive(this)
}

fun Iterable<*>.asJsonArray(): JsonArray {
  val list = this.map {
    return@map when (it) {
      null -> JsonNull
      is Number -> JsonLiteral(it)
      is String -> JsonLiteral(it)
      is Boolean -> JsonLiteral(it)
      is JsonElement -> it
      else -> illegalState("Invalid json value")
    }
  }
  return JsonArray(list)
}

fun Map<String, JsonElement>.toJsonObject(): JsonObject {
  return JsonObject(this)
}

