package io.mverse.jsonschema

import assertk.assertions.containsAll
import lang.net.URI
import org.junit.Test

class SchemaExtensionsTest {
  @Test fun testAllProperties() {
    val schema = JsonSchema.schema("http://schema.org/test") {
      "EMAILS" required string
      "NUMBER" optional number
      "OBJECT" required schemaBuilder {
        "NAME" required string
        "MAIN" required URI("#/properties/OBJECT")
        "PHONE" optional string
        "ADDRESS" optional schemaBuilder {
          "STREET1" required string
          "STREET2" required string
        }
      }
    }.asDraft7()
    val allSchemaProps = schema.allProperties
    assertk.assert(allSchemaProps.keys).containsAll("/EMAILS", "/NUMBER", "/OBJECT/NAME", "/OBJECT/PHONE",
        "/OBJECT/ADDRESS/STREET1", "/OBJECT/ADDRESS/STREET2")
  }
}