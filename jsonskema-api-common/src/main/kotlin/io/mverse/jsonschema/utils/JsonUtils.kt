package io.mverse.jsonschema.utils

import lang.json.JsrNumber
import lang.json.JsrObject
import lang.json.JsrString
import lang.json.JsrValue
import lang.json.bigDecimalValue
import lang.json.contains
import lang.json.get
import lang.json.jkey
import lang.json.stringValue
import lang.json.unboxOrNull
import lang.logging.Logger
import lang.net.URI

object JsonUtils {
  private val log = Logger("io.mverse.jsonschema.utils.JsonUtils")

  fun extractIdFromObject(json: JsrObject, id: String = "\$id", vararg otherIdKeys: String): URI? {
    if (id.jkey in json) {
      return tryParseURI(json[id.jkey])
    }

    for (idKeyword in otherIdKeys.asIterable() + "id") {
      if (idKeyword.jkey in json) {
        return tryParseURI(json[idKeyword.jkey])
      }
    }
    return null
  }

  /**
   * Safely parses URI from a JsrValue.  Logs any URI parsing failure, but will not log if the JsrValue is
   * not a JsonString instance
   */
  fun tryParseURI(uriValue: JsrValue): URI? {
    return (uriValue as? JsrString)?.let {
      URI(it.stringValue)
    }
  }

  fun prettyPrintArgs(args: Iterable<Any>): Array<Any> {
    return args.map {
      when (it) {
        is JsrValue -> it.toString()
        else -> it
      }
    }.toTypedArray()
  }

  fun prettyPrintArgs(vararg args: Any): Array<Any> = prettyPrintArgs(args.asIterable())
}

/**
 * Checks that elements are lexically equivalent.  This is to handle the case that enum values that contain
 * numbers should not be considered equal if their lexical representation is different, eg:
 *
 * 1.0, 1, 1.00
 *
 * These would be equal mathematically, but should not be considered to be lexically equivalent.
 *
 */
fun JsrValue.equalsLexically(other: JsrValue) :Boolean =
    if (this is JsrNumber && other is JsrNumber) {
      this.bigDecimalValue == other.bigDecimalValue
    } else {
      val thisValue:JsrValue = this
      thisValue == other
    }
