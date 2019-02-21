package io.mverse.jsonschema.builder

import assertk.assert
import assertk.assertAll
import assertk.assertions.isEqualTo
import io.mverse.jsonschema.JsonSchemas
import io.mverse.jsonschema.keyword.JsonValueKeyword
import io.mverse.jsonschema.keyword.Keywords
import lang.json.jsrNumber
import lang.json.jsrString
import lang.net.toURI
import org.junit.Test

class MutableJsonSchemaTest {

  @Test fun testSetters_WithUpdater() {
    val mutable = MutableJsonSchema(JsonSchemas.schemaLoader, "http://mverse/mutableSchemas".toURI())
    mutable.set(Keywords.CONST, jsrString("yes"))

    assert(mutable.const).isEqualTo("yes")
    assert(mutable.constValue).isEqualTo(jsrString("yes"))
  }

  @Test fun testSetters_WithoutUpdater() {
    val mutable = MutableJsonSchema(JsonSchemas.schemaLoader, "http://mverse/mutableSchemas".toURI())
    mutable.set(Keywords.CONST, jsrString("yes"))

    assert(mutable.const).isEqualTo("yes")
    assert(mutable.constValue).isEqualTo(jsrString("yes"))
  }

  @Test fun testSetters() {
    val mutable = MutableJsonSchema(JsonSchemas.schemaLoader, "http://mverse/mutableSchemas".toURI())
    mutable.constValue = jsrString("constance")
    mutable.additionalProperties = false
    mutable.allItemsSchema {
      const = "allItems"
    }
    mutable.allOf {
      const = "allOf"
    }
    mutable.oneOf {
      const = "oneOf"
    }

    mutable.anyOf {
      const = "anyOf"
    }

    mutable.itemSchema {
      const = "items"
    }

    mutable.properties["property"] = {
      const = "property"
    }

    mutable.allItemsSchema {
      const = "allItems"
    }

    var count = 0
    mutable.enumValues { listOf(jsrNumber(count++)) }

    assertAll {
      assert(mutable.enumValues?.firstOrNull()).isEqualTo(jsrNumber(0))
      assert(mutable.enumValues?.firstOrNull()).isEqualTo(jsrNumber(1))
      assert(mutable.enumValues?.firstOrNull()).isEqualTo(jsrNumber(2))

      assert(mutable.anyOfSchemas.first().const).isEqualTo("anyOf")
      assert(mutable.oneOfSchemas.first().const).isEqualTo("oneOf")
      assert(mutable.allOfSchemas.first().const).isEqualTo("allOf")
      assert(mutable.itemSchemas.first().const).isEqualTo("items")
      assert(mutable.allItemSchema?.const).isEqualTo("allItems")
      assert(mutable.properties["property"].draft7().constValue).isEqualTo(jsrString("property"))
    }
  }
}
