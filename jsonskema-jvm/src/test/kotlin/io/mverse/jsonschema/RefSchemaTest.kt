package io.mverse.jsonschema

import io.mverse.jsonschema.impl.RefSchemaImpl
import lang.URI
import kotlin.test.Test

class RefSchemaTest {
  @Test
  fun testRefSchemaToVersion() {
    RefSchemaImpl(location = SchemaLocation.documentRoot("https://nonexistant.com/#/foo"),
        refURI = URI("https://nonexistant.com"),
        refSchema = jsonschema {
          constValueDouble(42.0)
        })
  }
}
