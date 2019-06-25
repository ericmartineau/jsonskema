package io.mverse.jsonschema.loading

import assertk.assert
import assertk.assertThat
import assertk.assertions.hasSize
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.assertEquals

import io.mverse.jsonschema.Draft6Schema
import io.mverse.jsonschema.Schema
import org.junit.Assert
import org.junit.Test

/**
 * @author erosb
 */
class CombinedSchemaFactoryTest : BaseLoaderTest("combinedtestschemas.json") {

  @Test
  fun combinedSchemaLoading() {
    val actual = getSchemaForKey("combinedSchema")
    Assert.assertNotNull(actual)
  }

  @Test
  fun combinedSchemaWithBaseSchema() {
    val actual = getSchemaForKey("combinedSchemaWithBaseSchema")
    assertEquals(2, actual.anyOfSchemas.size.toLong())
  }

  @Test
  fun combinedSchemaWithExplicitBaseSchema() {
    val actual = getSchemaForKey("combinedSchemaWithExplicitBaseSchema")
    assertThat(actual.anyOfSchemas).hasSize(2)
  }

  @Test
  fun combinedSchemaWithMultipleBaseSchemas() {
    val actual = getSchemaForKey("combinedSchemaWithMultipleBaseSchemas")
    assertThat(actual.anyOfSchemas).hasSize(2)
  }
}
