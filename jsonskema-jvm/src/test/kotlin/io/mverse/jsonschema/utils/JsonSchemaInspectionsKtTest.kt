package io.mverse.jsonschema.utils

import assertk.assert
import assertk.assertions.hasToString
import lang.net.toURI
import org.junit.Test

class JsonSchemaInspectionsKtTest {
  @Test fun testExtractName() {
    val uri = "https://someschemas.com/mverse-test/mverse/booyards/0.1.2/schema/wardgets.json".toURI()
    val merge = "https://someschemas.com/mverse-test/mverse/booyards/0.1.2/schema/goobers.json"
        .toURI()
    assert(uri.calculateMergeURI(merge))
        .hasToString("https://someschemas.com/mverse-test/mverse/booyards/0.1.2/schema/wardgets-goobers.json")
  }

  @Test fun testExtractName_Invalid() {
    val uri = "https://someschemas.com/mverse-test/mverse/booyards/0.1.2/schema/wardgets.json".toURI()
    val merge = "https://different-base.com/mverse-test/mverse/booyards/0.1.2/schema/goobers.json"
        .toURI()
    assert {
      uri.calculateMergeURI(merge)
    }.thrownError { }
  }

  @Test fun testExtractName_Invalid_NoPath() {
    val uri = "https://someschemas.com/mverse-test/mverse/booyards/0.1.2/schema/wardgets.json".toURI()
    val merge = "https://different-base.com/someschemas.com"
        .toURI()
    assert {
      uri.calculateMergeURI(merge)
    }.thrownError { }
  }

  @Test fun testExtractName_Invalid_Identical() {
    val uri = "https://someschemas.com/mverse-test/mverse/booyards/0.1.2/schema/wardgets.json".toURI()
    val merge = "https://someschemas.com/mverse-test/mverse/booyards/0.1.2/schema/wardgets.json"
        .toURI()
    assert {
      uri.calculateMergeURI(merge)
    }.thrownError { }
  }
}
