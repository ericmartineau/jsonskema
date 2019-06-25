package io.mverse.jsonschema.enums

import assertk.assert
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.parse
import kotlinx.serialization.stringify
import lang.json.JSON
import org.junit.Test

class JsonSchemaTypeTest {
//  @Test
  @UseExperimental(ImplicitReflectionSerializer::class)
  fun testSerialization() {
    val serialized = JSON.stringify(JsonSchemaType.NUMBER)
    assertThat(serialized).isEqualTo("\"number\"")
    val jsonType = JSON.parse<JsonSchemaType>("number")
    val upperJsonType = JSON.parse<JsonSchemaType>("NUMBER")
    assertThat(jsonType).isEqualTo(JsonSchemaType.NUMBER)
    assertThat(upperJsonType).isEqualTo(JsonSchemaType.NUMBER)
  }
}
