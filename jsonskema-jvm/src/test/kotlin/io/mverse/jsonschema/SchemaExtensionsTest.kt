package io.mverse.jsonschema

import assertk.assertions.containsAll
import lang.net.URI
import org.junit.Test

class SchemaExtensionsTest {
  @Test fun testAllProperties() {
    val schema = schema(id = "http://schema.org/test") {
      properties {
        "EMAILS" required string
        "NUMBER" optional number
        "OBJECT" required {
          properties {
            "NAME" required string
            "MAIN" required URI("#/properties/OBJECT")
            "PHONE" optional string
            "ADDRESS" optional {
              properties {
                "STREET1" required string
                "STREET2" required string
              }
            }
          }

        }
      }
    }
    val allSchemaProps = schema.asDraft7().allProperties
    assertk.assert(allSchemaProps.keys).containsAll("/EMAILS", "/NUMBER", "/OBJECT/NAME", "/OBJECT/PHONE",
        "/OBJECT/ADDRESS/STREET1", "/OBJECT/ADDRESS/STREET2")
  }
}