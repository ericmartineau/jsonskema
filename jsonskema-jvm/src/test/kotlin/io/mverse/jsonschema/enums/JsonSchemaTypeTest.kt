package io.mverse.jsonschema.enums

import assertk.assert
import assertk.assertions.isEqualTo
import kotlinx.serialization.json.JSON
import org.junit.Test

class JsonSchemaTypeTest {
  @Test
  fun testSerialization() {
    val serialized = JSON.stringify(JsonSchemaType.NUMBER)
    assert(serialized).isEqualTo("\"number\"")
    val jsonType = JSON.parse<JsonSchemaType>("number")
    val upperJsonType = JSON.parse<JsonSchemaType>("NUMBER")
    assert(jsonType).isEqualTo(JsonSchemaType.NUMBER)
    assert(upperJsonType).isEqualTo(JsonSchemaType.NUMBER)
  }
}
