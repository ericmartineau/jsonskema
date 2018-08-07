package io.mverse.jsonschema

import assertk.all
import assertk.assert
import assertk.assertAll
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import kotlin.test.Test

class JsonPathTest {

  @Test
  fun testGetLastPath_WhenPathIsBlank_ThenReturnsNull() {
    assert(JsonPath.rootPath().lastPath)
        .isNull()
  }

  @Test
  fun testGetLastPath_WhenPathHasOneItem_ThenReturnsOnlyItem() {
    assert(JsonPath.rootPath().child("cookie").lastPath)
        .isNotNull {
          it.isEqualTo("cookie")
        }
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
    assertPath("From parsed JsonPointer", JsonPath.parseJsonPointer(jsonPath.toJsonPointer()!!))

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
