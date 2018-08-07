package io.mverse.jsonschema

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.json
import lang.json.toJson
import lang.json.asJsonArray

object TestUtils {

  fun createValue(value: JsonElement): JsonValueWithPath {
    return JsonValueWithPath.fromJsonValue(value)
  }

  fun createJsonObjectWithLocation(): JsonValueWithPath {
    val json = json {
      "foo" to "bar"
      "num" to 3
    }
    return JsonValueWithPath.fromJsonValue(json)
  }

  fun createJsonNumberWithLocation(number: Number): JsonValueWithPath {
    val jsonElement = number.toJson()
    return JsonValueWithPath.fromJsonValue(jsonElement)
  }

  fun createJsonStringWithLocation(string: String): JsonValueWithPath {
    val jsonElement = string.toJson()
    return JsonValueWithPath.fromJsonValue(jsonElement)
  }

  fun createJsonArrayWithLocation(): JsonValueWithPath {
    val jsonElement = listOf( "foo", "bar", 3, true, JsonNull).asJsonArray()

    return JsonValueWithPath.fromJsonValue(jsonElement)
  }
}
