package io.mverse.jsonschema.enums

import assertk.Assert
import assertk.assert
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull

class FormatTypeTest {

  @kotlin.test.Test
  fun testFromFormat_JsonPointer() {
    val formatType = FormatType.fromFormat("json-pointer")
    assertThat(formatType).isNotNull { it: Assert<FormatType> ->
      it.isEqualTo(FormatType.JSON_POINTER)
    }
  }

  @kotlin.test.Test
  fun testFromFormat_Null() {
    assertThat(FormatType.fromFormat(null)).isNull()
  }

  @kotlin.test.Test
  fun testFromFormat_Invalid() {
    assertThat(FormatType.fromFormat("non-existent-type")).isNull()
  }
}
