package io.mverse.jsonschema.assertj.asserts

import assertk.Assert
import assertk.assertions.isNotNull
import assertk.fail

fun Assert<String?>.isEqualIgnoringWhitespace(other:String?) {
  assert(actual).isNotNull()
  if(actual!!.stripWhitespace() != other?.stripWhitespace()) {
    fail("Expecting match (ignoring whitespace)\n" +
        " - ACTUAL: ${actual!!.prependIndent("   ")}\n" +
        " - EXPECTED: ${other!!.prependIndent("   ")}")
  }
}

fun String.stripWhitespace():String {
  return this.replace(Regex("\\s"), "")
}
