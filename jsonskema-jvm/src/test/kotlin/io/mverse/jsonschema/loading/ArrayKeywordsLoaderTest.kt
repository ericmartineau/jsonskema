package io.mverse.jsonschema.loading

import assertk.assert
import assertk.assertions.containsAll
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.assertj.asserts.asserting
import io.mverse.jsonschema.enums.JsonSchemaType
import org.junit.Test

/**
 * @author ericmartineau
 */
class ArrayKeywordsLoaderTest : BaseLoaderTest("arraytestschemas.json") {

  @Test
  fun arrayByAdditionalItems() {
    val actual = getSchemaForKey("arrayByAdditionalItems")
    assert(actual.additionalItemsSchema?.types).isNotNull {
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
    val actual = getSchemaForKey("arraySchema").asDraft6()
    assert(actual.minItems).isEqualTo(2)
    assert(actual.maxItems).isEqualTo(3)
    assert(actual.allItemSchema).isEqualTo(NULL_SCHEMA)
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
    private val NULL_SCHEMA = JsonSchema.schema {
      type = JsonSchemaType.NULL
    }
  }
}
