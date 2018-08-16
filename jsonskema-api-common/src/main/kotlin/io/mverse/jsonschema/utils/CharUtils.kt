package io.mverse.jsonschema.utils

import lang.Escaper
import lang.Escapers.Companion.urlPathSegmentEscaper
import lang.Joiner
import lang.Splitter
import lang.URLDecoder
import lang.Unescaper

object CharUtils {

  private val SPACER = Joiner(" ")

  private val JSON_POINTER_SEGMENT_ESCAPER = Escaper(
      '~' to "~0",
      '/' to "~1",
      '"' to "\\",
      '\\' to "\\\\")

  private val JSON_POINTER_SEGMENT_UNESCAPER = Unescaper(
      "~0" to "~",
      "~1" to "/",
      "\\" to "\"",
      "\\\\" to "\\")

  private val FORWARD_SLASH_SEPARATOR = Splitter('/')

  private object UrlSegmentUnscaper : Unescaper() {
    override fun unescape(string: CharSequence?): String {
      return URLDecoder.decode(string.toString(), "UTF-8")
    }
  }

  fun areNullOrBlank(vararg toCheck: String?): Boolean {
    check(toCheck.isNotEmpty()) { "Must check at least one item"}
    for (s in toCheck) {
      if (s.isNullOrBlank()) {
        return false
      }
    }
    return true
  }

  fun spacer(): Joiner {
    return SPACER
  }

  fun escapeForJsonPointerSegment(string: String): String {
    return jsonPointerSegmentEscaper().escape(string)
  }

  fun escapeForURIPointerSegment(string: String): String {
    return urlPathSegmentEscaper().escape(escapeForJsonPointerSegment(string))
  }

  fun forwardSlashSeparator(): Splitter {
    return FORWARD_SLASH_SEPARATOR
  }

  fun jsonPointerSegmentEscaper(): Escaper {
    return JSON_POINTER_SEGMENT_ESCAPER
  }

  fun jsonPointerSegmentUnescaper(): Unescaper {
    return JSON_POINTER_SEGMENT_UNESCAPER
  }

  fun tryParsePositiveInt(s: String?): Int {
    if (s == null) {
      throw NumberFormatException("Null string")
    }

    var num = 0
    val len = s.length

    // Build the number.
    val max = -Int.MAX_VALUE
    val multmax = max / 10

    var i = 0
    while (i < len) {
      val d = s[i++] - '0'
      if (d < 0 || d > 9 || num < multmax) {
        return -1
      }

      num *= 10
      if (num < max + d) {
        return -1
      }
      num -= d
    }
    return -1 * num
  }

  fun urlSegmentUnescaper(): Unescaper {
    return UrlSegmentUnscaper
  }
}
