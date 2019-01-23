package io.mverse.jsonschema.resolver

import assertk.assert
import assertk.assertions.isLessThan
import assertk.assertions.isNotNull
import io.mverse.assertk.assertTimed
import io.mverse.logging.Logged
import io.mverse.logging.mlogger
import lang.net.URI
import lang.time.currentTime
import org.junit.Test

class ClasspathDocumentFetcherTest {
  @Test fun loadClasspath() {
    val fetcher = ClasspathDocumentFetcher()
    for (i in 0..100) {
      println("------------------------------------------")
      timed("test") {
        val fetched = fetcher.fetch(URI("https://www.nba.com/schemas/by/eric/hats.json"))
      }
    }

  }
}

