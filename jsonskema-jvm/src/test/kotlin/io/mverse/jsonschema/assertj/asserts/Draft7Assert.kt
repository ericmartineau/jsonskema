package io.mverse.jsonschema.assertj.asserts

import assertk.assertions.isFalse
import assertk.assertions.isTrue
import io.mverse.jsonschema.keyword.Keywords

fun SchemaAssert.isReadOnly(): SchemaAssert {
  isDraft7().transform("readOnly") { it.isReadOnly }.isTrue()
  return this
}

fun SchemaAssert.isNotReadOnly(): SchemaAssert {
  isDraft7().transform("readOnly") { it.isReadOnly }.isFalse()
  return this
}

fun SchemaAssert.isWriteOnly(): SchemaAssert {
  isDraft7().transform("writeOnly") { it.isWriteOnly }.isTrue()
  return this
}

fun SchemaAssert.isNotWriteOnly(): SchemaAssert {
  isDraft7().transform("writeOnly") { it.isWriteOnly }.isFalse()
  return this
}

fun SchemaAssert.hasIfSchema(): SchemaAssert {
  hasKeyword(keyword = Keywords.IF)
  return transform { it.draft7().ifSchema!! }
}

fun SchemaAssert.hasThenSchema(): SchemaAssert {
  hasKeyword(keyword = Keywords.THEN)
  return transform { it.draft7().thenSchema!! }
}

fun SchemaAssert.hasElseSchema(): SchemaAssert {
  hasKeyword(keyword = Keywords.ELSE)
  return transform { it.draft7().elseSchema!! }
}


