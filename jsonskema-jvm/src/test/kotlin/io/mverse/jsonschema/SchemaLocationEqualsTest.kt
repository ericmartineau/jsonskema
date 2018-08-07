package io.mverse.jsonschema

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

class SchemaLocationEqualsTest {
  @Test
  fun testEquals() {
    EqualsVerifier.forClass(SchemaLocation::class.java)
        .withOnlyTheseFields("documentURI", "jsonPath", "resolutionScope")
        .suppress(Warning.STRICT_INHERITANCE)
        .verify()
  }
}
