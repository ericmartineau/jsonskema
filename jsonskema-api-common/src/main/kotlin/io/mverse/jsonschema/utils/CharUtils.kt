package io.mverse.jsonschema.utils

object CharUtils {

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
}
