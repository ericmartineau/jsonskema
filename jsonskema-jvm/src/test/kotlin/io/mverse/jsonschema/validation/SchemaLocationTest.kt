package io.mverse.jsonschema.validation

import io.mverse.jsonschema.SchemaLocation.Companion.documentRoot
import lang.URI
import kotlin.test.Test
import kotlin.test.assertEquals

class SchemaLocationTest {

  @Test
  fun testLocation() {

    val schemaLocation = documentRoot("http://mywebsite.com/schemas/core/primitives.json")
    val intermediateLocation = schemaLocation.child(URI("entities"), "definitions", "core", "platformEntity")
    val childLocation = intermediateLocation.child(URI("#platformIdentifier"), "properties", "id")

    assertEquals("JSON Pointer Correct", "/definitions/core/platformEntity/properties/id", childLocation.jsonPath.toJsonPointer())
    assertEquals("Child Canonical URL", "http://mywebsite.com/schemas/core/entities#platformIdentifier", childLocation.uniqueURI.toString())
    assertEquals("Child Resolution Scope", "http://mywebsite.com/schemas/core/entities#platformIdentifier", childLocation.resolutionScope.toString())
    assertEquals("Child Relative URL", "#/definitions/core/platformEntity/properties/id", childLocation.jsonPointerFragment.toString())
  }
}
