package io.mverse.jsonschema.assertj.asserts

import assertk.Assert
import assertk.assertions.isNotNull
import assertk.fail

fun Assert<String?>.isEqualIgnoringWhitespace(other:String?) {
  assert(actual).isNotNull()
  val strippedActual = actual!!.stripWhitespace()
  val strippedOther = other?.stripWhitespace()
  if(strippedActual != strippedOther) {
    fail("Expecting match (ignoring whitespace)\n" +
        " - ACTUAL: ${actual!!.prependIndent("   ")}\n" +
        " - EXPECTED: ${other!!.prependIndent("   ")}\n" +
        " - COMPARE1: $strippedOther\n" +
        " - COMPARE2: $strippedActual"
    )
  }
}

fun String.stripWhitespace():String {
  return this.replace(Regex("\\s"), "")
}
