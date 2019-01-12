package io.mverse.jsonschema.validation

import assertk.assertions.isFalse
import assertk.assertions.isTrue
import io.mverse.jsonschema.assertThat
import io.mverse.jsonschema.loading.parseKtJson
import kotlinx.serialization.json.JsonLiteral
import org.junit.Test

class ObjectComparatorTest {

  @Test
  fun testLexicalEquivalentForNumbers() {
    val testNumA = "1.00".parseKtJson() as JsonLiteral
    val testNumB = "1.0".parseKtJson() as JsonLiteral
    val testNumC = "1".parseKtJson() as JsonLiteral
    val testNumD = "1".parseKtJson() as JsonLiteral

    assertk.assert {
      testNumA.equalsLexically(testNumB).assertThat("1.00 lexical equivalent to 1.0").isFalse()
      testNumA.equalsLexically(testNumC).assertThat("1.00 lexical equivalent to 1").isFalse()
      testNumB.equalsLexically(testNumC).assertThat("1.0 lexical equivalent to 1").isFalse()
      testNumC.equalsLexically(testNumD).assertThat("1 lexical equivalent to 1").isTrue()
    }
  }
}
