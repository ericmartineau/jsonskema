package io.mverse.jsonschema.utils

import assertk.assert
import assertk.assertions.isNotNull
import kotlinx.io.core.readText
import lang.coroutines.blocking
import lang.string.toString
import org.junit.Test
import java.net.URI

class HttpGetKtTest {
  @Test fun getHttpGet(): Unit = blocking {
    val text = URI("https://storage.googleapis.com/mverse-test/mverse/slick/1.0.0/schema/contact/jsonschema-draft7.json")
        .httpGet().toString(Charsets.UTF_8)
    assert(text).isNotNull()
  }
}
