package io.mverse.jsonschema.loading

import assertk.assert
import assertk.assertThat
import assertk.assertions.hasToString
import io.mverse.jsonschema.JsonSchemas
import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.resourceLoader
import lang.json.JsrObject
import lang.json.jsrObject
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @author erosb
 */
class JsonObjectTest : BaseLoaderTest("objecttestcases.json") {

  private val testSchemas: JsrObject

  init {
    testSchemas = JsonSchemas.resourceLoader().readJsonObject("loading/testschemas.json")
  }

  @Test
  fun testHasKey() {
    assertTrue(subject().containsKey("a"))
  }

  private fun subject(): JsrObject {
    return jsrObject {
      "a" *= true
      "b" *= jsrObject {}
    }
  }

  @Test
  fun nestedId() {
    val schema = getJsonObjectForKey("nestedId")
    val schemaJson = JsonValueWithPath.fromJsonValue(schema)

    val (_, _, location) = schemaJson.path(Keywords.PROPERTIES).path("prop")
    assertThat(location.canonicalURI).hasToString("http://x.y/z#zzz")
  }

  @Test
  fun childForConsidersIdAttr() {
    val input = testSchemas["remotePointerResolution"] as JsrObject
    val fc = input["properties"]?.asJsonObject()?.getJsonObject("folderChange")!!
    fc.getJsonObject("properties").getJsonObject("schemaInFolder")
    //Assertion?
  }
}
