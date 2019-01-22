package io.mverse.jsonschema.loading.reference

import assertk.assert
import assertk.assertions.contains
import assertk.assertions.hasSize
import assertk.assertions.isBetween
import assertk.assertions.isInstanceOf
import assertk.assertions.isLessThan
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import assertk.assertions.message
import io.mverse.assertk.assertTimed
import io.mverse.assertk.hasStringValue
import io.mverse.assertk.hasValueAtPointer
import io.mverse.jsonschema.assertj.asserts.isEqualIgnoringWhitespace
import io.mverse.jsonschema.resolver.ClasspathDocumentFetcher
import io.mverse.jsonschema.resolver.DocumentFetchException
import io.mverse.jsonschema.resolver.FetchedDocument
import io.mverse.jsonschema.resolver.HttpDocumentFetcher
import io.mverse.jsonschema.resolver.JsonDocumentFetcher
import kotlinx.coroutines.delay
import lang.net.URI
import org.junit.Test
import java.io.FileNotFoundException
import java.util.concurrent.TimeoutException

class DefaultJsonDocumentClientTest {
  @Test fun testFetchingNoProvidersFound() {
    assert {
      DefaultJsonDocumentClient(listOf())
    }.thrownError { }
  }

  @Test fun testFetchingAllProvidersFailed() {
    val defaultClient = DefaultJsonDocumentClient()
    assertTimed {
      defaultClient.fetchDocument("https://nba.com/no/document/here")
    }.run {
      duration().isLessThan(5000)
      verify {

        thrownError {
          isInstanceOf(DocumentFetchException::class) {
            assert(it.actual.failures).hasSize(2)
            assert(it.actual.failures[ClasspathDocumentFetcher::class]).isNotNull {
              it.message().isNotNull {
                it.contains("No resource on classpath")
              }
            }

            assert(it.actual.failures[HttpDocumentFetcher::class]).isNotNull {
              it.isInstanceOf(FileNotFoundException::class) {
                it.message().isNotNull {
                  it.contains("https://www.nba.com/no/document/here")
                }
              }
            }
          }
        }
      }
    }
  }

  @Test fun testFetchingOneProviderSucceeded() {
    val defaultClient = DefaultJsonDocumentClient()
    val fetched = defaultClient.fetchDocument("https://nba.com/schemas/by/eric/hats.json").fetchedDocument
    assert(fetched.isDifferentURI).isTrue()
    assert(fetched.schemaData).isEqualIgnoringWhitespace("{}")
    assert(fetched.jsrObject).hasValueAtPointer("/\$id") {
      hasStringValue("https://nba.com/schemas/by/eric/hats.json")
    }
  }

  @Test fun testFetchingCancelledFetcher() {
    val defaultClient = DefaultJsonDocumentClient()
    defaultClient += SlowpokeFetcher()

    assertTimed {
      val fetched = defaultClient.fetchDocument("https://nba.com/schemas/by/eric/hats.json").fetchedDocument
      assert(fetched.isDifferentURI).isTrue()
      assert(fetched.schemaData).isEqualIgnoringWhitespace("{}")
      assert(fetched.jsrObject).hasValueAtPointer("/\$id") {
        hasStringValue("https://nba.com/schemas/by/eric/hats.json")
      }
    }.duration().isLessThan(9000)
  }

  @Test fun testFetchingSlowpokeTimeout() {
    val defaultClient = DefaultJsonDocumentClient(listOf(SlowpokeFetcher()), fetchTimeout = 500)
    assertTimed {
      defaultClient.fetchDocument("https://unknown.com/path/is/bogus")
    }.apply {
      verify {
        thrownError {
          isInstanceOf(DocumentFetchException::class) {
            assert(it.actual.failures).hasSize(1)
          }
        }
      }
      duration().isBetween(500, 6000)
    }
  }

  class SlowpokeFetcher : JsonDocumentFetcher {
    override suspend fun fetchDocument(uri: URI): FetchedDocument {
      delay(10000)
      throw TimeoutException("Died after 10 seconds")
    }
  }
}
