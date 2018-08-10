package io.mverse.jsonschema

import assertk.Assert
import assertk.assert
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.json
import lang.illegalState
import lang.json.toJsonLiteral
import lang.json.toJsonArray
import kotlin.test.assert

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
    val jsonElement = number.toJsonLiteral()
    return JsonValueWithPath.fromJsonValue(jsonElement)
  }

  fun createJsonStringWithLocation(string: String): JsonValueWithPath {
    val jsonElement = string.toJsonLiteral()
    return JsonValueWithPath.fromJsonValue(jsonElement)
  }

  fun createJsonArrayWithLocation(): JsonValueWithPath {
    val jsonElement = listOf( "foo", "bar", 3, true, JsonNull).toJsonArray()

    return JsonValueWithPath.fromJsonValue(jsonElement)
  }
}

inline fun <reified T:Any?> T.assertThat(message:String? = null): Assert<T> = assert(this, message)
inline fun <reified T, reified V> T?.assertThat(block: T.()->V): Assert<V> = assert(this?.block() ?: illegalState("Accessor method returned null"))
