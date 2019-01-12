package io.mverse.jsonschema.impl

import assertk.assert
import assertk.assertions.hasToString
import assertk.assertions.isEqualTo
import io.mverse.jsonschema.Draft3Schema
import io.mverse.jsonschema.Draft4Schema
import io.mverse.jsonschema.Draft6Schema
import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.enums.JsonSchemaVersion
import kotlinx.serialization.json.JsonPrimitive
import lang.net.URI
import org.junit.Test

class JsonSchemaImplTest {

  @Test
  fun testWithIdDraft7() {
    val theSchema = JsonSchema.schema("https://www.schema.org/foo")
    assert(theSchema.id).hasToString("https://www.schema.org/foo")
    val newURI = URI("https://google.com/hosted/foo/blah")
    val withId = theSchema.withId(newURI)
    assert(withId.id).isEqualTo(newURI)
  }

  @Test
  fun testWithIdDraft6() {
    val theSchema: Draft6Schema = JsonSchema.schema("https://www.schema.org/foo").asDraft6()
    assert(theSchema.id).hasToString("https://www.schema.org/foo")
    val newURI = URI("https://google.com/hosted/foo/blah")
    val withId = theSchema.withId(newURI).asDraft6()
    assert(withId.id).isEqualTo(newURI)
  }

  @Test
  fun testWithIdDraft4() {
    val theSchema: Draft4Schema = JsonSchema.schema("https://www.schema.org/foo").asDraft4()
    assert(theSchema.id).hasToString("https://www.schema.org/foo")
    val newURI = URI("https://google.com/hosted/foo/blah")
    val withId = theSchema.withId(newURI).asDraft4()
    assert(withId.id).isEqualTo(newURI)
  }

  @Test
  fun testWithIdDraft3() {
    val theSchema: Draft3Schema = JsonSchema.schema("https://www.schema.org/foo").asDraft3()
    assert(theSchema.id).hasToString("https://www.schema.org/foo")
    val newURI = URI("https://google.com/hosted/foo/blah")
    val withId = theSchema.withId(newURI).asDraft3()
    assert(withId.id).isEqualTo(newURI)
  }

  @Test
  fun testToStringExtraProperties() {
    val schema = JsonSchema.schema {
      extraProperties += ("bobTheBuilder" to JsonPrimitive(true))

      properties["childSchema"] = schemaBuilder {
        extraProperties += ("bobsType" to JsonPrimitive("Chainsaw Murderer"))

        properties["grandchildSchema"] = schemaBuilder {
          extraProperties += ("theNestStatus" to JsonPrimitive("FULL"))
        }
      }
    }

    val schemaString = schema.toString(includeExtraProperties = true, version=JsonSchemaVersion.Draft7)
    assert(schemaString).isEqualTo("{\"properties\":{\"childSchema\":{\"properties\":{\"grandchildSchema\":{\"theNestStatus\":\"FULL\"}},\"bobsType\":\"Chainsaw Murderer\"}},\"bobTheBuilder\":true}")
  }

  @Test
  fun testToStringNoExtraProperties() {
    val schema = JsonSchema.schema {
      extraProperties += ("bobTheBuilder" to JsonPrimitive(true))

      properties["childSchema"] = schemaBuilder {
        extraProperties += ("bobsType" to JsonPrimitive("Chainsaw Murderer"))

        properties["grandchildSchema"] = schemaBuilder {
          extraProperties += ("theNestStatus" to JsonPrimitive("FULL"))
        }
      }
    }

    val schemaString = schema.toString(includeExtraProperties = false, version=JsonSchemaVersion.Draft7)
    assert(schemaString).isEqualTo("{\"properties\":{\"childSchema\":{\"properties\":{\"grandchildSchema\":{}}}}}")
  }
}
