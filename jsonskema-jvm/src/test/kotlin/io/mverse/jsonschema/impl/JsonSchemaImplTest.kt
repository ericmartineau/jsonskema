package io.mverse.jsonschema.impl

import assertk.assert
import assertk.assertions.hasToString
import assertk.assertions.isEqualTo
import io.mverse.jsonschema.Draft3Schema
import io.mverse.jsonschema.Draft4Schema
import io.mverse.jsonschema.Draft6Schema
import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.schema
import io.mverse.jsonschema.versionedSchema
import lang.URI
import org.junit.Test

class JsonSchemaImplTest {

  @Test
  fun testWithIdDraft7() {
    val theSchema = JsonSchema.schema("https://www.schema.org/foo") {}
    assert(theSchema.id).hasToString("https://www.schema.org/foo")
    val newURI = URI("https://google.com/hosted/foo/blah")
    val withId = theSchema.withId(newURI)
    assert(withId.id).isEqualTo(newURI)
  }

  @Test
  fun testWithIdDraft6() {
    val theSchema: Draft6Schema = JsonSchema.versionedSchema("https://www.schema.org/foo") {}
    assert(theSchema.id).hasToString("https://www.schema.org/foo")
    val newURI = URI("https://google.com/hosted/foo/blah")
    val withId = theSchema.withId(newURI).asDraft6()
    assert(withId.id).isEqualTo(newURI)
  }

  @Test
  fun testWithIdDraft4() {
    val theSchema: Draft4Schema = JsonSchema.versionedSchema("https://www.schema.org/foo") {}
    assert(theSchema.id).hasToString("https://www.schema.org/foo")
    val newURI = URI("https://google.com/hosted/foo/blah")
    val withId = theSchema.withId(newURI).asDraft4()
    assert(withId.id).isEqualTo(newURI)
  }

  @Test
  fun testWithIdDraft3() {
    val theSchema: Draft3Schema = JsonSchema.versionedSchema("https://www.schema.org/foo") {}
    assert(theSchema.id).hasToString("https://www.schema.org/foo")
    val newURI = URI("https://google.com/hosted/foo/blah")
    val withId = theSchema.withId(newURI).asDraft3()
    assert(withId.id).isEqualTo(newURI)
  }
}
