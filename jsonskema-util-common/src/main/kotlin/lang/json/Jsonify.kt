package lang.json

import kotlinx.serialization.context.SimpleModule
import kotlinx.serialization.json.JSON
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonLiteral
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import lang.illegalState


val kjson = JSON.nonstrict

enum class ElementType {
  NUMBER,
  BOOLEAN,
  ARRAY,
  STRING,
  OBJECT,
  NULL
}

fun String?.toJsonLiteral(): JsonPrimitive {
  return JsonPrimitive(this)
}

fun Number?.toJsonLiteral(): JsonPrimitive {
  return JsonPrimitive(this)
}

fun Boolean?.toJsonLiteral(): JsonPrimitive {
  return JsonPrimitive(this)
}

fun jsonArrayOf(vararg elements:Any?): JsonArray = elements.toList().toJsonArray()

fun Map<*, *>.toJsonObject(): JsonObject {
  return when(this) {
    is JsonObject -> this
    else -> JsonObject(this
        .filterKeys { it != null }
        .map { (k, v) -> k!!.toString() to v.toJsonElement() }
        .toMap())
  }
}

fun Iterable<*>.toJsonArray(): JsonArray {
  val list = this.map {
    return@map when (it) {
      null -> JsonNull
      is Number -> JsonLiteral(it)
      is String -> JsonLiteral(it)
      is Boolean -> JsonLiteral(it)
      is JsonElement -> it
      is Iterable<*>-> it.toJsonArray()
      is Map<*, *>-> it.toJsonObject()
      is Enum<*>-> it.toString().toJsonLiteral()
      is JsrValue-> it.toJsonElKt()
      else -> illegalState("Invalid json value ${it::class}")
    }
  }
  return JsonArray(list)
}

fun Any?.toJsonElement(): JsonElement {
  return when (this) {
      null -> JsonNull
      is Number -> JsonLiteral(this)
      is String -> JsonLiteral(this)
      is Boolean -> JsonLiteral(this)
      is JsonElement -> this
      is Iterable<*>-> this.toJsonArray()
      is Map<*, *>-> this.toJsonObject()
      is Enum<*>-> this.toString().toJsonLiteral()
      is JsrValue -> this.toJsonElKt()
      else -> illegalState("Invalid json value: ${this::class}")
    }
  }

operator fun JsonObject.get(pointer: JsonPointer): JsonElement {
  val iterator = pointer.iterator()
  return this[iterator]
}

operator fun JsonObject.get(parts: Iterator<String>): JsonElement {
  if (!parts.hasNext()) {
    return this
  }
  val firstPath = parts.next()
  val found = if(containsKey(firstPath)) this[firstPath] else JsonNull

  return when {
    found is JsonObject -> found[parts]
    found is JsonArray -> found[parts]
    parts.hasNext() -> JsonNull // There is further path resolution, but we've hit a non-structure
    else -> found
  }
}

operator fun JsonArray.get(pointer: JsonPointer): JsonElement {
  val iterator = pointer.iterator()
  return this[iterator]
}

operator fun JsonArray.get(parts: Iterator<String>): JsonElement {
  if (!parts.hasNext()) {
    return this
  }
  val firstPath = parts.next()
  val idx = firstPath.toIntOrNull() ?: illegalState("Array path requires int, but found $firstPath")
  val found = if(size > idx) this[idx] else JsonNull

  return when {
    found is JsonObject -> found[parts]
    found is JsonArray -> found[parts]
    parts.hasNext() -> JsonNull // There is further path resolution, but we've hit a non-structure
    else -> found
  }
}

