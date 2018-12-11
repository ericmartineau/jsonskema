package lang.json

import kotlinx.serialization.json.ElementType
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonLiteral
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import lang.illegalState

fun JsrObject.toJsonObjectKt(): JsonObject = this.properties
    .map { (k, value) -> k to value.toJsonElKt() }
    .toMap()
    .toJsonObject()

fun JsrArray.toJsonArrayKt(): JsonArray {
  return this.values.map(JsrValue::toJsonElKt).toJsonArray()
}

fun JsrValue.toJsonElKt(): JsonElement = when (this) {
  JsrNull -> JsonNull
  JsrTrue -> JsonLiteral(true)
  JsrFalse -> JsonLiteral(false)
  is JsrObject -> this.toJsonObjectKt()
  is JsrArray -> this.toJsonArrayKt()
  is JsrString -> JsonLiteral(this.stringValue)
  is JsrNumber -> JsonLiteral(this.numberValue)
  else -> illegalState("Unknown JsonValue type: $this")
}

fun JsonObject.toJsonObjectJsr(): JsrObject {
  val values = entries
      .map { (k, value) -> k to value.toJsonValueJsr() }
      .toMap()

  return createJsrObject(values)
}

fun JsonElement.toJsonValueJsr(): JsrValue = when (this) {
  JsonNull -> JsrNull
  is JsonObject -> this.toJsonObjectJsr()
  is JsonArray -> this.toJsonArrayJsr()
  is JsonLiteral -> when (this.type) {
    ElementType.NUMBER -> when (this.number) {
      is Int -> createJsrNumber(this.number as Int)
      is Long -> createJsrNumber(this.number as Long)
      else -> createJsrNumber(this.number.toDouble())
    }
    ElementType.STRING -> createJsrString(this.toString())
    ElementType.BOOLEAN -> when (this.boolean) {
      true -> JsrTrue
      false -> JsrFalse
    }

    else -> illegalState("Unable to convert json literal $this")
  }
}

fun JsonArray.toJsonArrayJsr(): JsrArray {
  return createJsrArray(this.map(JsonElement::toJsonValueJsr))
}
