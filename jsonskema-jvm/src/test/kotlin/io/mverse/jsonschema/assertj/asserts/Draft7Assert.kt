package io.mverse.jsonschema.assertj.asserts

import assertk.Assert
import assertk.all
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import io.mverse.jsonschema.Draft7Schema
import io.mverse.jsonschema.keyword.Keywords

fun Assert<Draft7Schema>.isReadOnly(): Assert<Draft7Schema> {
  assert(actual.isReadOnly, "readOnly").all {
    isNotNull()
    isTrue()
  }
  return this
}

fun Draft7Assert.isNotReadOnly(): Assert<Draft7Schema> {
  assert(actual.isReadOnly, "readOnly").all {
    isNotNull()
    isFalse()
  }
  return this
}

fun Draft7Assert.isWriteOnly(): Assert<Draft7Schema> {
  assert(actual.isWriteOnly, "writeOnly").all {
    isNotNull()
    isTrue()
  }
  return this
}

fun Draft7Assert.isNotWriteOnly(): Assert<Draft7Schema> {
  assert(actual.isWriteOnly, "writeOnly").all {
    isNotNull()
    isTrue()
  }
  return this
}

fun Draft7Assert.hasIfSchema(): Draft7Assert {
  hasKeyword(keyword = Keywords.IF)
  return assert(actual.ifSchema!!.asDraft7())
}

fun Draft7Assert.hasThenSchema(): Draft7Assert {
  hasKeyword(keyword = Keywords.THEN)
  return assert(actual.thenSchema!!.asDraft7())
}

fun Assert<Draft7Schema>.hasElseSchema(): Assert<Draft7Schema> {
  hasKeyword(keyword = Keywords.ELSE)
  return assert(actual.elseSchema!!.asDraft7())
}


