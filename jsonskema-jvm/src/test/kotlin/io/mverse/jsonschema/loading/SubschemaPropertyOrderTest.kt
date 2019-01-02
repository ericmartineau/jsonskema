package io.mverse.jsonschema.loading

import assertk.assert
import assertk.assertions.isEqualTo
import lang.URI
import org.junit.Test

class SubschemaPropertyOrderTest {
  @Test fun testDefinitionSchemaPropertyOrder() {
    val contactSchema = SchemaLoaderImpl().readSchema(URI("https://storage.googleapis.com/mverse-test/mverse/slick/1.0.0/schema/contact/jsonschema-draft7.json"))
    val contactDef = contactSchema.asDraft7().definitions["contact"]!!.asDraft7()

    assert(contactDef.properties.keys.toList()).isEqualTo(listOf("givenName", "familyName", "email", "phone", "timeZone"))
  }
}