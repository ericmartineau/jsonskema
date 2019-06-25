package io.mverse.jsonschema.loading.reference

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.hasSize
import assertk.assertions.isBetween
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isLessThan
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import assertk.assertions.message
import io.ktor.client.features.ResponseException
import io.mverse.assertk.hasStringValue
import io.mverse.assertk.hasValueAtPointer
import io.mverse.jsonschema.assertj.asserts.isEqualIgnoringWhitespace
import io.mverse.jsonschema.resolver.ClasspathDocumentFetcher
import io.mverse.jsonschema.resolver.FetchedDocument
import io.mverse.jsonschema.resolver.HttpDocumentFetcher
import io.mverse.jsonschema.resolver.JsonDocumentFetcher
import io.mverse.logging.mlogger
import io.mverse.test.assertTimed
import lang.exception.illegalState
import lang.net.URI
import lang.time.currentTime
import org.junit.Test

const val slowpoke = 2000L
const val expectedSlowpoke = 2000L

class DefaultJsonDocumentClientTest {
  @Test fun testFetchingNoProvidersFound() {
    assertThat {
      DefaultJsonDocumentClient(mutableListOf(), schemaCache = JsonSchemaCache())
    }.thrownError { }
  }

  @Test fun testFetchingAllProvidersFailed() {
    val defaultClient = DefaultJsonDocumentClient(2000)
    assertTimed {
      defaultClient.fetchDocument("https://nba.com/no/document/here")
    }
        .duration { isLessThan(2100) }
        .returnedValue { result ->
          result.transform { it.fetchedOrNull }.isNull()
          result.transform { it.failures }.hasSize(2)
          result.transform { it.failures[ClasspathDocumentFetcher::class] }.isNotNull()
              .message()
              .isNotNull()
              .contains("No resource on classpath")

          result.transform { it.failures[HttpDocumentFetcher::class] }.isNotNull()
              .isInstanceOf(ResponseException::class)
              .message()
              .isNotNull()
              .contains("404 Not Found")

        }
  }

  @Test fun testFetchingOneProviderSucceeded() {
    val defaultClient = DefaultJsonDocumentClient()
    val fetched = defaultClient.fetchDocument("https://nba.com/schemas/by/eric/hats.json").fetched
    assertThat(fetched.isDifferentURI).isTrue()
    assertThat(fetched.schemaData).isEqualIgnoringWhitespace("{}")
    assertThat(fetched.jsrObject).hasValueAtPointer("/\$id") {
      hasStringValue("https://nba.com/schemas/by/eric/hats.json")
    }
  }

  @Test fun testFetchingCancelledFetcher() {
    val defaultClient = DefaultJsonDocumentClient()
    defaultClient += SlowpokeFetcher()

    assertTimed {
      val fetched = defaultClient.fetchDocument("https://nba.com/schemas/by/eric/hats.json").fetched
      assertThat(fetched.isDifferentURI).isTrue()
      assertThat(fetched.schemaData).isEqualIgnoringWhitespace("{}")
      assertThat(fetched.jsrObject).hasValueAtPointer("/\$id") {
        hasStringValue("https://nba.com/schemas/by/eric/hats.json")
      }
    }.duration { isLessThan(expectedSlowpoke) }
  }

  @Test fun testFetchingSlowpokeTimeout_Single() {
    val defaultClient = DefaultJsonDocumentClient(SlowpokeFetcher(), fetchTimeout = 500)
    assertTimed {
      defaultClient.fetchDocument("https://unknown.com/path/is/bogus")
    }
        .returnedValue { results ->
          results.transform { it.failures }.hasSize(0)
          results.transform { it.fetchedOrNull }.isNull()
        }
        .duration { isBetween(500, expectedSlowpoke) }
  }

  @Test fun testFetchingSlowpokeTimeout_Multi() {
    val defaultClient = DefaultJsonDocumentClient(SlowpokeFetcher(), SlowpokeFetcher(), fetchTimeout = 500)
    assertTimed {
      defaultClient.fetchDocument("https://unknown.com/path/is/bogus")
    }
        .returnedValue { results ->
          results.transform { it.failures }.hasSize(0)
          results.transform { it.fetchedOrNull }.isNull()
        }
        .duration { isBetween(500, expectedSlowpoke) }
  }

  @Test fun testLoadingClasspath() {
    val client = DefaultJsonDocumentClient(ClasspathDocumentFetcher())
    client.fetchDocument("https://storage.googleapis.com/mverse-test/schemas/schema.json")
    assertTimed {
      val time = currentTime()
      val r = client.fetchDocument("https://storage.googleapis.com/mverse-test/schemas/schema.json")
      println("Delay ${currentTime() - time}ms")
      r
    }
        .doesNotThrowAnyException()
        .returnedValue { returned ->
          returned.isNotNull()
              .transform { it.fetchedOrNull!!.schemaData }
              .isEqualTo("{}")
        }
        .duration { isLessThan(200) }
  }

  class SlowpokeFetcher : JsonDocumentFetcher {
    override suspend fun fetchDocument(uri: URI): FetchedDocument {
      val start = currentTime()
      try {
        Thread.sleep(slowpoke)
        throw IllegalStateException("Slowpoke puked after ${slowpoke / 1000} seconds")
      } catch (e: InterruptedException) {
        illegalState("Slowpoke was too slow ${currentTime() - start}ms")
      }
    }

    override val isInterruptable = true

    companion object {
      val log = mlogger {}
    }
  }
}

