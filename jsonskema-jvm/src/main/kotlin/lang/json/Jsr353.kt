package lang.json

import javax.json.JsonNumber
import javax.json.JsonString
import javax.json.JsonValue
import javax.json.spi.JsonProvider

actual typealias JsrValue = javax.json.JsonValue
actual typealias JsrStructure = javax.json.JsonStructure

actual typealias JsrNumber = JsonNumber
actual val JsrNumber.numberValue: Number get() = this.numberValue()

actual typealias JsrString = JsonString
actual val JsrString.stringValue: String get() = this.string

actual typealias JsrObject = javax.json.JsonObject
actual val JsrObject.properties: Set<Map.Entry<String, JsrValue>> get() = this.entries

actual typealias JsrArray = javax.json.JsonArray
actual val JsrArray.values: List<JsrValue> get() = this

actual val JsrNull: JsonValue = JsonValue.NULL
actual val JsrTrue: JsonValue = JsonValue.TRUE
actual val JsrFalse: JsonValue = JsonValue.FALSE

val provider: JsonProvider get() = JsonProvider.provider()

actual fun createJsrObject(values: Map<String, JsrValue>): JsrObject {
  return provider.createObjectBuilder(values).build()
}

actual fun createJsrArray(values: Iterable<JsrValue>): JsrArray {
  return provider.createArrayBuilder(values.toList()).build()
}

actual fun createJsrString(string: String): JsrString = provider.createValue(string)
actual fun createJsrNumber(int: Int): JsrNumber = provider.createValue(int)
actual fun createJsrNumber(double: Double): JsrNumber = provider.createValue(double)
actual fun createJsrNumber(long: Long): JsrNumber = provider.createValue(long)

