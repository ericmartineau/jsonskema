package io.mverse.jsonschema.utils

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isSameAs
import assertk.assertions.isTrue
import kotlinx.serialization.json.json
import lang.json.toJsonObject
import lang.json.toKtArray
import lang.net.URI
import lang.net.isFragmentOnly
import lang.net.resolveUri
import kotlin.test.Test

class URIUtilsTest {
  @Test
  fun withNewFragment_FromURN_FragmentAppended() {
    val uriToTest = URI("urn:jsonschema:com:zzzzz:tests:commons:jsonschema:models:Person")
    assert(uriToTest.withNewFragment(URI("#/some/pointer")))
        .isEqualTo(URI("urn:jsonschema:com:zzzzz:tests:commons:jsonschema:models:Person#/some/pointer"))
  }

  @Test
  fun resolve_AgainstURN_WithFragmentOnly() {
    val uriToTest = URI("urn:jsonschema:com:zzzzz:tests:commons:jsonschema:models:Person")
    assert(uriToTest.resolveUri(URI("#/some/pointer")))
        .isEqualTo(URI("urn:jsonschema:com:zzzzz:tests:commons:jsonschema:models:Person#/some/pointer"))
  }

  @Test
  fun withNewFragment_FromURI_FragmentAppended() {
    val uriToTest = URI("https://www.mysite.com/some/url.html#/oldpath/to/stuff")
    assert(uriToTest.withNewFragment(URI("#/new/path")))
        .isEqualTo(URI("https://www.mysite.com/some/url.html#/new/path"))
  }

  @Test
  fun withoutFragment_WithFragment_FragmentRemoved() {
    val uriToTest = URI("http://www.coolsite.com/items?foo=bob#/some/pointer")
    assert(uriToTest.withoutFragment())
        .isEqualTo(URI("http://www.coolsite.com/items?foo=bob"))
  }

  @Test
  fun withoutFragment_EmptyFragment() {
    val uriToTest = URI("http://www.coolsite.com/items?foo=bob#")
    assert(uriToTest.withoutFragment())
        .isEqualTo(URI("http://www.coolsite.com/items?foo=bob"))
  }

  @Test
  fun withoutFragment_WhenNoFragment_ThenReturnsSame() {
    val uriToTest = URI("http://www.coolsite.com/items?foo=bob")
    assert(uriToTest.withoutFragment())
        .isSameAs(uriToTest)
  }

  @Test
  fun withoutFragment_WhenBlankURI_Blank() {
    val uriToTest = URI("")
    assert(uriToTest.withoutFragment())
        .isSameAs(uriToTest)
  }

  @Test
  fun withoutFragment_WhenOnlyFragment_ReturnBlank() {
    val uriToTest = URI("#")
    assert(uriToTest.withoutFragment())
        .isEqualTo(URI(""))
  }

  @Test
  fun isFragment_WhenFullHttpURL_ReturnsFalse() {
    val uriToTest = URI("http://www.coolsite.com/items?foo=bob#/some/pointer")
    assert(uriToTest.isFragmentOnly).isFalse()
  }

  @Test
  fun isFragment_WhenBlank_ReturnsFalse() {
    val uriToTest = URI("")
    assert(uriToTest.isFragmentOnly).isFalse()
  }

  @Test
  fun isFragment_WhenEmptyFragment_ReturnsTrue() {
    val uriToTest = URI("#")
    assert(uriToTest.isFragmentOnly).isTrue()
  }

  @Test
  fun isFragment_WhenNonPointer_ReturnsTrue() {
    val uriToTest = URI("#identifier")
    assert(uriToTest.isFragmentOnly).isTrue()
  }

  @Test
  fun isFragment_WhenQueryAndFragment_ReturnsFalse() {
    val uriToTest = URI("?foo=true#path")
    assert(uriToTest.isFragmentOnly).isFalse()
  }

  @Test
  fun isJsonPointerFragment_WhenQueryAndFragment_ReturnsFalse() {
    val uriToTest = URI("?foo=true#/path/to")
    assert(uriToTest.isJsonPointer()).isFalse()
  }

  @Test
  fun isJsonPointerFragment_WhenEmpty_ReturnsTrue() {
    val uriToTest = URI("#")
    assert(uriToTest.isJsonPointer()).isTrue()
  }

  @Test
  fun isJsonPointerFragment_WhenForwardSlash_ReturnsTrue() {
    val uriToTest = URI("#/")
    assert(uriToTest.isJsonPointer()).isTrue()
  }

  @Test
  fun generateAbsoluteURI() {
    val jsonObject = json {
      "bob" to "jones"
      "age" to 34
      "sub" to listOf(3, 5, 67).toKtArray()
    }

    val uri = generateUniqueURI(jsonObject)
    val resolve = uri.resolveUri("#/foofy")
    assert(resolve.toString()).isEqualTo(uri.toString() + "#/foofy")
  }

  @Test
  fun generateUniqueURI_ForSameRootObject_ReturnsSameURI() {
    val jsonObject = json {
      "bob" to "jones"
      "age" to 34
      val numbers = listOf<Number>(3, 5, 67)
      "sub" to numbers.toKtArray()
    }

    val uri = generateUniqueURI(jsonObject)
    val fromString = jsonObject.toJsonObject()
    assert(uri).isEqualTo(generateUniqueURI(fromString))
  }
}
