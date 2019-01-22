package io.mverse.jsonschema

import assertk.assertions.isTrue
import io.mverse.assertk.hasStringValue
import io.mverse.assertk.hasValueAtPointer
import io.mverse.jsonschema.resolver.ClasspathDocumentFetcher
import lang.coroutine.blocking
import lang.net.toURI
import org.junit.Test

class FetchingTest {
  @Test fun testFetchingClasspath() {
    val cp = ClasspathDocumentFetcher()
    val fetched = blocking {
      cp.fetchDocument("http://nba.com/schemas/by/eric/hats.json".toURI())
    }
    assertk.assert(fetched.isDifferentURI).isTrue()
    assertk.assert(fetched.jsrObject).hasValueAtPointer("/\$id") {
      hasStringValue("http://nba.com/schemas/by/eric/hats.json")
    }
  }
}