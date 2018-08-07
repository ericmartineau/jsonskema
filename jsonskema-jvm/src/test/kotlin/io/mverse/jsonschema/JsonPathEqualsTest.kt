package io.mverse.jsonschema

import nl.jqno.equalsverifier.EqualsVerifier
import nl.jqno.equalsverifier.Warning
import org.junit.Test

class JsonPathEqualsTest {

  @Test
  fun testEquals() {
    EqualsVerifier.forClass(JsonPath::class.java)
        .withOnlyTheseFields("segments")
        .suppress(Warning.STRICT_INHERITANCE)
        .verify()
  }
}
