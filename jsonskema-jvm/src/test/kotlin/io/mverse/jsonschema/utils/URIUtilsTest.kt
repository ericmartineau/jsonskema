package io.mverse.jsonschema.utils

import assertk.assert
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isSameAs
import assertk.assertions.isTrue
import kotlinx.serialization.json.json
import lang.json.jsrArray
import lang.json.jsrArrayOf
import lang.json.jsrObject
import lang.json.toKtArray
import lang.net.URI
import lang.net.isFragmentOnly
import lang.net.resolveUri
import lang.net.withNewFragment
import kotlin.test.Test

class URIUtilsTest {
  @Test
  fun withNewFragment_FromURN_FragmentAppended() {
    val uriToTest = URI("urn:jsonschema:com:zzzzz:tests:commons:jsonschema:models:Person")
    assertThat(uriToTest.withNewFragment(URI("#/some/pointer")))
        .isEqualTo(URI("urn:jsonschema:com:zzzzz:tests:commons:jsonschema:models:Person#/some/pointer"))
  }

  @Test
  fun resolve_AgainstURN_WithFragmentOnly() {
    val uriToTest = URI("urn:jsonschema:com:zzzzz:tests:commons:jsonschema:models:Person")
    assertThat(uriToTest.resolveUri(URI("#/some/pointer")))
        .isEqualTo(URI("urn:jsonschema:com:zzzzz:tests:commons:jsonschema:models:Person#/some/pointer"))
  }

  @Test
  fun withNewFragment_FromURI_FragmentAppended() {
    val uriToTest = URI("https://www.mysite.com/some/url.html#/oldpath/to/stuff")
    assertThat(uriToTest.withNewFragment(URI("#/new/path")))
        .isEqualTo(URI("https://www.mysite.com/some/url.html#/new/path"))
  }

  @Test
  fun withoutFragment_WithFragment_FragmentRemoved() {
    val uriToTest = URI("http://www.coolsite.com/items?foo=bob#/some/pointer")
    assertThat(uriToTest.withoutFragment())
        .isEqualTo(URI("http://www.coolsite.com/items?foo=bob"))
  }

  @Test
  fun withoutFragment_EmptyFragment() {
    val uriToTest = URI("http://www.coolsite.com/items?foo=bob#")
    assertThat(uriToTest.withoutFragment())
        .isEqualTo(URI("http://www.coolsite.com/items?foo=bob"))
  }

  @Test
  fun withoutFragment_WhenNoFragment_ThenReturnsSame() {
    val uriToTest = URI("http://www.coolsite.com/items?foo=bob")
    assertThat(uriToTest.withoutFragment())
        .isSameAs(uriToTest)
  }

  @Test
  fun withoutFragment_WhenBlankURI_Blank() {
    val uriToTest = URI("")
    assertThat(uriToTest.withoutFragment())
        .isSameAs(uriToTest)
  }

  @Test
  fun withoutFragment_WhenOnlyFragment_ReturnBlank() {
    val uriToTest = URI("#")
    assertThat(uriToTest.withoutFragment())
        .isEqualTo(URI(""))
  }

  @Test
  fun isFragment_WhenFullHttpURL_ReturnsFalse() {
    val uriToTest = URI("http://www.coolsite.com/items?foo=bob#/some/pointer")
    assertThat(uriToTest.isFragmentOnly).isFalse()
  }

  @Test
  fun isFragment_WhenBlank_ReturnsFalse() {
    val uriToTest = URI("")
    assertThat(uriToTest.isFragmentOnly).isFalse()
  }

  @Test
  fun isFragment_WhenEmptyFragment_ReturnsTrue() {
    val uriToTest = URI("#")
    assertThat(uriToTest.isFragmentOnly).isTrue()
  }

  @Test
  fun isFragment_WhenNonPointer_ReturnsTrue() {
    val uriToTest = URI("#identifier")
    assertThat(uriToTest.isFragmentOnly).isTrue()
  }

  @Test
  fun isFragment_WhenQueryAndFragment_ReturnsFalse() {
    val uriToTest = URI("?foo=true#path")
    assertThat(uriToTest.isFragmentOnly).isFalse()
  }

  @Test
  fun isJsonPointerFragment_WhenQueryAndFragment_ReturnsFalse() {
    val uriToTest = URI("?foo=true#/path/to")
    assertThat(uriToTest.isJsonPointer()).isFalse()
  }

  @Test
  fun isJsonPointerFragment_WhenEmpty_ReturnsTrue() {
    val uriToTest = URI("#")
    assertThat(uriToTest.isJsonPointer()).isTrue()
  }

  @Test
  fun isJsonPointerFragment_WhenForwardSlash_ReturnsTrue() {
    val uriToTest = URI("#/")
    assertThat(uriToTest.isJsonPointer()).isTrue()
  }

  @Test
  fun generateAbsoluteURI() {
    val jsonObject = jsrObject {
      "bob" to "jones"
      "age" to 34
      "sub" to listOf(3, 5, 67).toKtArray()
    }

    val uri = generateUniqueURI(jsonObject)
    val resolve = uri.resolveUri("#/foofy")
    assertThat(resolve.toString()).isEqualTo(uri.toString() + "#/foofy")
  }

  @Test
  fun generateUniqueURI_ForSameRootObject_ReturnsSameURI() {
    val jsonObject = jsrObject {
      "bob" *= "jones"
      "age" *= 34
      "sub" *= jsrArrayOf(3, 5, 67)
    }

    val uri = generateUniqueURI(jsonObject)
    assertThat(uri).isEqualTo(generateUniqueURI(jsonObject))
  }
}
