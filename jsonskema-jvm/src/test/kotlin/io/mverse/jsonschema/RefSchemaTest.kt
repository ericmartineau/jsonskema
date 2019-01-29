package io.mverse.jsonschema

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import io.mverse.jsonschema.assertj.asserts.asserting
import io.mverse.jsonschema.assertj.asserts.hasKeyword
import io.mverse.jsonschema.assertj.asserts.isEqualIgnoringWhitespace
import io.mverse.jsonschema.assertj.asserts.isVersion
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.impl.RefJsonSchema
import io.mverse.jsonschema.keyword.Keywords
import lang.json.jsrJson
import lang.net.URI
import kotlin.test.Test

class RefSchemaTest {

  @Test
  fun testRefSchemaToVersion() {
    jsrJson {
      val withRef = RefJsonSchema(JsonSchemas.schemaReader.loader, location = SchemaLocation.documentRoot("https://nonexistant.com/#/foo"),
          refURI = URI("https://nonexistant.com"),
          refSchema = schema {
            constValue = 42.0.toJsrJson()
          })

      withRef.asDraft4()
          .asserting()
          .isVersion(JsonSchemaVersion.Draft4)
          .hasKeyword(Keywords.CONST)
    }
  }

  /**
   * Make sure this doesn't stack overflow
   */
  @Test
  fun testRefSchemaNullToVersion() {
    val withRef = RefJsonSchema(JsonSchemas.schemaReader.loader, location = SchemaLocation.documentRoot("https://nonexistant.com/#/foo"),
        refURI = URI("https://nonexistant.com"))

    assert(withRef.toString(true)).isEqualIgnoringWhitespace("{\"\$ref\": \"https://nonexistant.com\"}")
  }

  /**
   * Make sure this doesn't stack overflow
   */
  @Test
  fun testRefResolutionWithBuilders() {
    val  childSchema = schema(id = URI("http://schemas/child")) {
      properties {
        "parent" required URI("http://schemas/parent")
      }
    }

    val  parentSchema = schema(id = URI("http://schemas/parent")) {
      properties {
        "name" required string
      }
    }

    assert(JsonSchemas.schemaReader.readSchema(URI("http://schemas/parent"))).isEqualTo(parentSchema)
    assert(JsonSchemas.schemaReader.readSchema(URI("http://schemas/child"))).isEqualTo(childSchema)

    val childRef = childSchema.asDraft7().properties["parent"]?.asDraft7()
    assert(childRef).isNotNull {
        assert(it.actual).isInstanceOf(RefSchema::class) {
          assert(it.actual.refSchema).isEqualTo(parentSchema)
        }
    }
  }
}
