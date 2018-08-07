package io.mverse.jsonschema.enums

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull

class FormatTypeTest {

  @kotlin.test.Test
  fun testFromFormat_JsonPointer() {
    val formatType = FormatType.fromFormat("json-pointer")
    assert(formatType).isNotNull {
      it.isEqualTo(FormatType.JSON_POINTER)
    }
  }

  @kotlin.test.Test
  fun testFromFormat_Null() {
    assert(FormatType.fromFormat(null)).isNull()
  }

  @kotlin.test.Test
  fun testFromFormat_Invalid() {
    assert(FormatType.fromFormat("non-existent-type")).isNull()
  }
}
