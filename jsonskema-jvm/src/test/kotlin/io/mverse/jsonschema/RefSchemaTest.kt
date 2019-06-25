package io.mverse.jsonschema

import assertk.Assert
import assertk.assert
import assertk.assertThat
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
import lang.json.toJsrValue
import lang.net.URI
import kotlin.test.Test

class RefSchemaTest {

  @Test
  fun testRefSchemaToVersion() {

    val withRef = RefJsonSchema(JsonSchemas.schemaReader.loader, location = SchemaLocation.documentRoot("https://nonexistant.com/#/foo"),
        refURI = URI("https://nonexistant.com"),
        refSchema = schema {
          constValue = 42.0.toJsrValue()
        })

    withRef.draft4()
        .asserting()
        .isVersion(JsonSchemaVersion.Draft4)
        .hasKeyword(Keywords.CONST)
  }

  /**
   * Make sure this doesn't stack overflow
   */
  @Test
  fun testRefSchemaNullToVersion() {
    val withRef = RefJsonSchema(JsonSchemas.schemaReader.loader,
        location = SchemaLocation.documentRoot("https://nonexistant.com/#/foo"),
        refURI = URI("https://nonexistant.com"))

    assertThat<String>(withRef.toString(true)).isEqualIgnoringWhitespace("{\"\$ref\": \"https://nonexistant.com\"}")
  }

  /**
   * Make sure this doesn't stack overflow
   */
  @Test
  fun testRefResolutionWithBuilders() {
    val childSchema = schema(id = URI("http://schemas/child")) {
      properties {
        "parent" required URI("http://schemas/parent")
      }
    }

    val parentSchema = schema(id = URI("http://schemas/parent")) {
      properties {
        "name" required string
      }
    }

    assertThat(JsonSchemas.schemaReader.readSchema(URI("http://schemas/parent"))).isEqualTo(parentSchema)
    assertThat(JsonSchemas.schemaReader.readSchema(URI("http://schemas/child"))).isEqualTo(childSchema)

    val childRef = childSchema.draft7().properties["parent"]?.draft7()
    assertThat(childRef).isNotNull()
        .isInstanceOf(RefSchema::class)
        .transform { it.refSchema}
        .isEqualTo(parentSchema)
  }
}
