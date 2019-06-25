package io.mverse.jsonschema.loading

import assertk.Assert
import assertk.assert
import assertk.assertThat
import assertk.assertions.containsAll
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import io.mverse.jsonschema.assertj.asserts.asserting
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.schema
import org.junit.Test

/**
 * @author ericmartineau
 */
class ArrayKeywordsLoaderTest : BaseLoaderTest("arraytestschemas.json") {

  @Test
  fun arrayByAdditionalItems() {
    val actual = getSchemaForKey("arrayByAdditionalItems")
    assertThat(actual.additionalItemsSchema?.draft7()?.types).isNotNull { it: Assert<Set<JsonSchemaType>> ->
      it.containsAll(JsonSchemaType.NULL)
    }
  }

  @Test
  fun arrayByItems() {
    getSchemaForKey("arrayByItems")
        .asserting()
        .isNotNull()
  }

  @Test
  fun arraySchema() {
    val actual = getSchemaForKey("arraySchema").draft6()
    assertThat(actual.minItems).isEqualTo(2)
    assertThat(actual.maxItems).isEqualTo(3)
    assertThat(actual.allItemSchema).isEqualTo(NULL_SCHEMA)
  }

  @Test
  fun invalidAdditionalItems() {
    getJsonObjectForKey("invalidAdditionalItems")
        .assertAsSchema()
        .failedAt(schemaLocation = "#/additionalItems")
  }

  @Test
  fun invalidArrayItemSchema() {
    getJsonObjectForKey("invalidArrayItemSchema")
        .assertAsSchema()
        .failedAt(schemaLocation = "#/items/0")
  }

  @Test
  fun invalidItemsJsonSchema() {
    getJsonObjectForKey("invalidItemsArraySchema")
        .assertAsSchema()
        .failedAt(schemaLocation = "#/items")
  }

  companion object {
    private val NULL_SCHEMA = schema {
      type = JsonSchemaType.NULL
    }
  }
}
