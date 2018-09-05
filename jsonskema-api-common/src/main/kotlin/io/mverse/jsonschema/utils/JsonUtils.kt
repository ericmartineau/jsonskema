package io.mverse.jsonschema.utils

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonLiteral
import kotlinx.serialization.json.JsonObject
import lang.Logger
import lang.URI

object JsonUtils {
  private val log = Logger("io.mverse.jsonschema.utils.JsonUtils")

  fun extractIdFromObject(json: JsonObject, id: String = "\$id", vararg otherIdKeys: String): URI? {
    if (json.containsKey(id)) {
      return tryParseURI(json[id])
    }

    for (idKeyword in otherIdKeys.asIterable() + "id") {
      if (json.containsKey(idKeyword)) {
        return tryParseURI(json[idKeyword])
      }
    }
    return null
  }

  /**
   * Safely parses URI from a JsonElement.  Logs any URI parsing failure, but will not log if the JsonElement is
   * not a JsonString instance
   */
  fun tryParseURI(uriValue: JsonElement): URI? {
    if (uriValue is JsonLiteral) {
      uriValue.contentOrNull
    }
    return when(uriValue) {
      is JsonLiteral-> try {
        URI(uriValue.contentOrNull)
      } catch(e:Exception) {
        log.warn("Failed to parse URI: ${uriValue.contentOrNull}")
        return null
      }
      else->null
    }
  }

  fun prettyPrintArgs(args: Iterable<Any>): Array<Any> {
    return args.map {
      when (it) {
        is JsonElement->it.toString()
        else-> it
      }
    }.toTypedArray()
  }

  fun prettyPrintArgs(vararg args: Any): Array<Any> = prettyPrintArgs(args.asIterable())
}
