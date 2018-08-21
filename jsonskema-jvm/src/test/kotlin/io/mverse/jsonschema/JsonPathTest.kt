package io.mverse.jsonschema

import assertk.all
import assertk.assert
import assertk.assertAll
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import kotlinx.serialization.json.JSON
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.json
import lang.json.toJsonArray
import lang.json.get
import kotlin.test.Test

class JsonPathTest {

  @Test
  fun `(lastPath) WHEN path is blank THEN return null`() {
    assert(JsonPath.rootPath().lastPath)
        .isNull()
  }

  @Test
  fun `(lastPath) WHEN path has exactly 1 item THEN return 1 item`() {
    assert(JsonPath.rootPath().child("cookie").lastPath)
        .isNotNull {
          it.isEqualTo("cookie")
        }
  }

  @Test
  fun testSerialize() {
    val path = JsonPath.parseJsonPointer("/bob/jones/richard")
    val serialized = JSON.stringify(path)
    assert(serialized).isEqualTo("\"/bob/jones/richard\"")
    val parsed = JSON.parse<JsonPath>("/bob/jones/richard")
    assert(parsed).isEqualTo(path)
  }


  @Test
  fun testGetLastPath_WhenPathHasMultipleItems_ThenReturnsLastItem() {
    assert(JsonPath.rootPath().child("chocolate", "chip", "cookie").lastPath)
        .isNotNull {
          it.isEqualTo("cookie")
        }
  }

  @Test
  fun testFirstPath_WhenPathHasMultipleItems_ThenReturnsFirstItem() {
    assert(JsonPath.rootPath().child("chocolate", "chip", "cookie").firstPath)
        .isNotNull {
          it.isEqualTo("chocolate")
        }
  }

  @Test
  fun testForEach_WhenJoinerIsSupplied_ThenJoinerIsApplied() {
    val path = JsonPath.rootPath().child("chocolate", "chip", "cookie")
    val joined = path.toStringPath().joinToString(".")
    assert(joined).isEqualTo("chocolate.chip.cookie")
  }

  @Test
  fun testParseURI_PathPartIsUrlEncodedSlash() {
    val uri = "#/this%2For%7E1that"
    val jsonPath = JsonPath.parseFromURIFragment(uri)
    assertPath("First Pass", jsonPath)
    assertPath("From parsed URI", JsonPath.parseFromURIFragment(jsonPath.toURIFragment()))
    assertPath("From parsed JsonPointer", JsonPath.parseJsonPointer(jsonPath.toJsonPointer()))

    val child = jsonPath.child("eric~is/not/so/bad")
    val message = "Child"
    assertAll {

      assert(child.toStringPath(), "$message: Unescaped raw").all {
        hasSize(2)
        containsExactly("this/or/that", "eric~is/not/so/bad")
      }

      assert(child.toURIFragment().toString(), "$message: Correct URL Encoding")
          .isEqualTo("#/this~1or~1that/eric~0is~1not~1so~1bad")

      assert(child.toJsonPointer(), "$message: Correct JSON-Pointer encoding")
          .isEqualTo("/this~1or~1that/eric~0is~1not~1so~1bad")
    }
  }

  @Test
  fun `(jsonPointer) WHEN valid nested pointer THEN return correct element`() {
    val testObject = json {
      "name" to json {
        "first" to "Eric"
        "last" to "Martineau"
      }
      "parts" to listOf(3, 4, json {
        "a" to "A"
        "b" to "B"
        "A-D" to listOf("A", "B", "C", "D").toJsonArray()
      }).toJsonArray()
    }

    val pointerToC = JsonPath.parseJsonPointer("/parts/2/A-D/2")
    val found = testObject.get(pointerToC)
    assertk.assert(found.contentOrNull).isEqualTo("C")
  }

  @Test
  fun `(jsonPointer) WHEN invalid nested pointer THEN return JsonNull`() {
    val testObject = json {
      "name" to json {
        "first" to "Eric"
        "last" to "Martineau"
      }
      "parts" to listOf(3, 4, json {
        "a" to "A"
        "b" to "B"
        "A-D" to listOf("A", "B", "C", "D").toJsonArray()
      }).toJsonArray()
    }

    val pointerToC = JsonPath.parseJsonPointer("/parts/5/A-D/2")
    val found = testObject.get(pointerToC)
    assert(found).isEqualTo(JsonNull)
  }

  @Test
  fun `(jsonPointer) WHEN invalid root pointer THEN return JsonNull`() {
    val testObject = json {
      "name" to json {
        "first" to "Eric"
        "last" to "Martineau"
      }
      "parts" to listOf(3, 4, json {
        "a" to "A"
        "b" to "B"
        "A-D" to listOf("A", "B", "C", "D").toJsonArray()
      }).toJsonArray()
    }

    val pointerToC = JsonPath.parseJsonPointer("/partsy")
    val found = testObject[pointerToC]
    assertk.assert(found).isEqualTo(JsonNull)
  }

  @Test
  fun `(jsonPointer) WHEN missing nested pointer THEN return JsonNull`() {
    val testObject = json {
      "name" to json {
        "first" to "Eric"
        "last" to "Martineau"
      }
      "parts" to listOf(3, 4, json {
        "a" to "A"
        "b" to "B"
        "A-D" to listOf("A", "B", "C", "D").toJsonArray()
      }).toJsonArray()
    }

    val pointerToC = JsonPath.parseJsonPointer("/name/middle")
    val found = testObject.get(pointerToC)
    assertk.assert(found).isEqualTo(JsonNull)
  }

  private fun assertPath(message: String, jsonPath: JsonPath) {
    assertAll {
      assert(jsonPath.toStringPath(), "$message: Unescaped raw").all {
        hasSize(1)
        containsExactly("this/or/that")
      }

      assert(jsonPath.toURIFragment().toString(), "$message: Correct URL Encoding")
          .isEqualTo("#/this~1or~1that")
      assert(jsonPath.toJsonPointer(), "$message: Correct JSON-Pointer encoding")
          .isEqualTo("/this~1or~1that")
    }
  }
}
