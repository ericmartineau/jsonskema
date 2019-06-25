package io.mverse.jsonschema.loading

import assertk.assert
import assertk.assertThat
import assertk.assertions.isEqualTo
import io.mverse.jsonschema.loading.reference.JsonSchemaCache
import lang.net.URI
import org.junit.Test

class SubschemaPropertyOrderTest {
  @Test fun testDefinitionSchemaPropertyOrder() {
    val contactSchema = SchemaLoaderImpl(JsonSchemaCache()).readSchema(URI("https://storage.googleapis.com/mverse-test/mverse/slick/1.0.0/schema/contact/jsonschema-draft7.json"))
    val contactDef = contactSchema.draft7().definitions.getValue("contact").draft7()

    assertThat(contactDef.properties.keys.toList()).isEqualTo(listOf("givenName", "familyName", "email", "phone", "timeZone"))
  }
}
