package io.mverse.jsonschema.loading

import assertk.assert
import assertk.assertions.hasToString
import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.resourceLoader
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.json
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

/**
 * @author erosb
 */
class JsonObjectTest : BaseLoaderTest("objecttestcases.json") {

  private val testSchemas: JsonObject

  init {
    testSchemas = JsonSchema.resourceLoader().readJsonObject("loading/testschemas.json")
  }

  @Test
  fun testHasKey() {
    assertTrue(subject().containsKey("a"))
  }

  private fun subject(): JsonObject {
    return json {
      "a" to true
      "b" to json{}
    }
  }

  @Test
  fun nestedId() {
    val schema = getJsonObjectForKey("nestedId")
    val schemaJson = JsonValueWithPath.fromJsonValue(schema)

    val (_, _, location) = schemaJson.path(Keywords.PROPERTIES).path("prop")
    assert(location.canonicalURI).hasToString("http://x.y/z#zzz")
  }

  @Test
  fun childForConsidersIdAttr() {
    val input = testSchemas.getObject("remotePointerResolution")
    val fc = input.getObject("properties").getObject("folderChange")
    val sIF  = fc.getObject("properties").getObject("schemaInFolder")
    //Assertion?
  }
}
