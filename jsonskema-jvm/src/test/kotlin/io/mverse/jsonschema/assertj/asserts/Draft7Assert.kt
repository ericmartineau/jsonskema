package io.mverse.jsonschema.assertj.asserts

import assertk.Assert
import assertk.all
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import io.mverse.jsonschema.Draft7Schema
import io.mverse.jsonschema.keyword.Keywords

fun SchemaAssert.isReadOnly(): SchemaAssert {
  assert(actual.asDraft7().isReadOnly, "readOnly").all {
    isNotNull()
    isTrue()
  }
  return this
}

fun SchemaAssert.isNotReadOnly(): SchemaAssert {
  assert(actual.asDraft7().isReadOnly, "readOnly").all {
    isNotNull()
    isFalse()
  }
  return this
}

fun SchemaAssert.isWriteOnly(): SchemaAssert {
  assert(actual.asDraft7().isWriteOnly, "writeOnly").all {
    isNotNull()
    isTrue()
  }
  return this
}

fun SchemaAssert.isNotWriteOnly(): SchemaAssert {
  assert(actual.asDraft7().isWriteOnly, "writeOnly").all {
    isNotNull()
    isTrue()
  }
  return this
}

fun SchemaAssert.hasIfSchema(): SchemaAssert {
  hasKeyword(keyword = Keywords.IF)
  return assert(actual.asDraft7().ifSchema!!)
}

fun SchemaAssert.hasThenSchema(): SchemaAssert {
  hasKeyword(keyword = Keywords.THEN)
  return assert(actual.asDraft7().thenSchema!!)
}

fun SchemaAssert.hasElseSchema(): SchemaAssert {
  hasKeyword(keyword = Keywords.ELSE)
  return assert(actual.asDraft7().elseSchema!!)
}


