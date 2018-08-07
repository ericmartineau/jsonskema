package io.mverse.jsonschema


import assertk.assert
import assertk.assertions.isEqualTo
import io.mverse.jsonschema.utils.SchemaPaths
import kotlin.test.Test

class KeyMissingExceptionTest {

  @Test
  fun testMessage() {
    val exception = KeyMissingException(SchemaPaths.fromNonSchemaSource(this), "bob")
    assert(exception.message).isEqualTo("#: Missing value at key [bob]")
  }
}
