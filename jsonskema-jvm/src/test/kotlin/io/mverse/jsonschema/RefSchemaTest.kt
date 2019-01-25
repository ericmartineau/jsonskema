package io.mverse.jsonschema

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import io.mverse.jsonschema.assertj.asserts.asserting
import io.mverse.jsonschema.assertj.asserts.hasKeyword
import io.mverse.jsonschema.assertj.asserts.isVersion
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.impl.RefSchemaImpl
import io.mverse.jsonschema.keyword.Keywords
import lang.json.jsrJson
import lang.net.URI
import kotlin.test.Test

class RefSchemaTest {
  @Test
  fun testRefSchemaToVersion() {
    jsrJson {
      val withRef = RefSchemaImpl(location = SchemaLocation.documentRoot("https://nonexistant.com/#/foo"),
          refURI = URI("https://nonexistant.com"),
          refSchema = JsonSchema.schema {
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
    val withRef = RefSchemaImpl(location = SchemaLocation.documentRoot("https://nonexistant.com/#/foo"),
        refURI = URI("https://nonexistant.com"))

    withRef.asDraft4()
        .asserting()
        .isVersion(JsonSchemaVersion.Draft4)
  }

  /**
   * Make sure this doesn't stack overflow
   */
  @Test
  fun testRefResolutionWithBuilders() {
    val loader = JsonSchema.schemaReader.loader
    val  childSchema = JsonSchema.schema("http://schemas/child") {
      schemaLoader = loader
      "parent" required URI("http://schemas/parent")
    }.asDraft7()

    val  parentSchema = JsonSchema.schema("http://schemas/parent") {
      schemaLoader = loader
      "name" required string
    }.asDraft7()

    assert(JsonSchema.schemaReader.readSchema(URI("http://schemas/parent"))).isEqualTo(parentSchema)
    assert(JsonSchema.schemaReader.readSchema(URI("http://schemas/child"))).isEqualTo(childSchema)

    val childRef = childSchema.properties["parent"]?.asDraft7()
    assert(childRef).isNotNull {
      it.isInstanceOf(RefSchema::class) {
        assert(it.actual.refSchema).isEqualTo(parentSchema)
      }

    }

  }
}
