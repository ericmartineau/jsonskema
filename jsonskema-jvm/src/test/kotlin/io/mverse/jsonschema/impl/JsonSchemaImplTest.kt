package io.mverse.jsonschema.impl

import assertk.assert
import assertk.assertions.hasToString
import assertk.assertions.isEqualTo
import io.mverse.jsonschema.jsonschema
import lang.URI
import org.junit.Test

class JsonSchemaImplTest {

  @Test
  fun testWithIdDraft7() {
    val theSchema = jsonschema("https://www.schema.org/foo") {}
    assert(theSchema.id).hasToString("https://www.schema.org/foo")
    val newURI = URI("https://google.com/hosted/foo/blah")
    val withId = theSchema.withId(newURI)
    assert(withId.id).isEqualTo(newURI)
  }

  @Test
  fun testWithIdDraft6() {
    val theSchema = jsonschema("https://www.schema.org/foo") {}.asDraft6()
    assert(theSchema.id).hasToString("https://www.schema.org/foo")
    val newURI = URI("https://google.com/hosted/foo/blah")
    val withId = theSchema.withId(newURI).asDraft6()
    assert(withId.id).isEqualTo(newURI)
  }

  @Test
  fun testWithIdDraft4() {
    val theSchema = jsonschema("https://www.schema.org/foo") {}.asDraft4()
    assert(theSchema.id).hasToString("https://www.schema.org/foo")
    val newURI = URI("https://google.com/hosted/foo/blah")
    val withId = theSchema.withId(newURI).asDraft4()
    assert(withId.id).isEqualTo(newURI)
  }

  @Test
  fun testWithIdDraft3() {
    val theSchema = jsonschema("https://www.schema.org/foo") {}.asDraft3()
    assert(theSchema.id).hasToString("https://www.schema.org/foo")
    val newURI = URI("https://google.com/hosted/foo/blah")
    val withId = theSchema.withId(newURI).asDraft3()
    assert(withId.id).isEqualTo(newURI)
  }
}
