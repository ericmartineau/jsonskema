package io.mverse.jsonschema.loading

import assertk.assert
import assertk.assertions.isNotNull
import org.junit.Test

/**
 * @author ericmartineau
 */
class StringKeywordsLoaderTest : BaseLoaderTest("stringtestschemas.json") {

  @Test
  fun patternSchema() {
    val actual = getSchemaForKey("patternSchema")
    assert(actual.pattern).isNotNull()
  }
}
