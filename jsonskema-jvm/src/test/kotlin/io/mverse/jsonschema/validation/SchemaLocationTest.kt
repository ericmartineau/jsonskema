package io.mverse.jsonschema.validation

import assertk.assert
import assertk.assertions.isEqualTo
import io.mverse.jsonschema.SchemaLocation.Companion.documentRoot
import lang.net.URI
import kotlin.test.Test

class SchemaLocationTest {

  @Test
  fun testLocation() {

    val schemaLocation = documentRoot("http://mywebsite.com/schemas/core/primitives.json")
    val intermediateLocation = schemaLocation.child(URI("entities"), "definitions", "core", "platformEntity")
    val childLocation = intermediateLocation.child(URI("#platformIdentifier"), "properties", "id")

    assert("/definitions/core/platformEntity/properties/id", "JSON Pointer Correct").isEqualTo(childLocation.jsonPath.jsonPtr)
    assert("http://mywebsite.com/schemas/core/entities#platformIdentifier", "Child Canonical URL").isEqualTo(childLocation.uniqueURI.toString())
    assert("http://mywebsite.com/schemas/core/entities#platformIdentifier", "Child Resolution Scope").isEqualTo(childLocation.resolutionScope.toString())
    assert("#/definitions/core/platformEntity/properties/id", "Child Relative URL").isEqualTo(childLocation.jsonPointerFragment.toString())
  }
}
