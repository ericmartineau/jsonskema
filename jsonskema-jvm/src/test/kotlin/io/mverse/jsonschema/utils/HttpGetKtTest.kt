package io.mverse.jsonschema.utils

import assertk.assertThat
import assertk.assertions.isNotNull
import lang.coroutines.blocking
import lang.string.toString
import org.junit.Test
import java.net.URI

class HttpGetKtTest {
  @Test fun getHttpGet() {
    blocking {
      val text = URI("https://storage.googleapis.com/mverse-test/mverse/slick/1.0.0/schema/contact/jsonschema-draft7.json")
          .httpGet().toString(Charsets.UTF_8)
      assertThat(text).isNotNull()
    }
  }
}
