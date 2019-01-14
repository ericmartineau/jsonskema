package io.mverse.jsonschema

import io.mverse.jsonschema.assertj.asserts.asserting
import io.mverse.jsonschema.assertj.asserts.hasKeyword
import io.mverse.jsonschema.assertj.asserts.isVersion
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.impl.RefSchemaImpl
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.loading.SchemaLoaderImpl
import lang.json.jsrJson
import lang.net.URI
import lang.json.toJsrValue
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
        refURI = URI("https://nonexistant.com"),
        refSchemaLoader = {null})

    withRef.asDraft4()
        .asserting()
        .isVersion(JsonSchemaVersion.Draft4)
  }
}
