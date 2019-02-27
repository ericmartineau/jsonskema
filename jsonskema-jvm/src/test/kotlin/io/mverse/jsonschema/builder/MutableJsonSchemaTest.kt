package io.mverse.jsonschema.builder

import assertk.assert
import assertk.assertAll
import assertk.assertions.contains
import assertk.assertions.doesNotContain
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import io.mverse.jsonschema.JsonSchemas
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.keyword.JsonValueKeyword
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.schema
import lang.json.jsrArrayOf
import lang.json.jsrNumber
import lang.json.jsrString
import lang.net.toURI
import org.junit.Test

class MutableJsonSchemaTest {
  @Test fun testSetters_WithUpdater() {
    val mutable = MutableJsonSchema(JsonSchemas.schemaLoader, "http://mverse/mutableSchemas".toURI())
    mutable[Keywords.CONST] = JsonValueKeyword("yes")

    assert(mutable.const).isEqualTo("yes")
    assert(mutable.constValue).isEqualTo(jsrString("yes"))
  }

  @Test fun testSetters_WithoutUpdater() {
    val mutable = MutableJsonSchema(JsonSchemas.schemaLoader, "http://mverse/mutableSchemas".toURI())
    mutable[Keywords.CONST] = JsonValueKeyword("yes")

    assert(mutable.const).isEqualTo("yes")
    assert(mutable.constValue).isEqualTo(jsrString("yes"))
  }

  @Test fun testRemoveProperty() {
    val schema = JsonSchemas.schema {
      properties {
        "name" required string
        "age" required number
      }
    }

    val withoutAge = schema.toMutableSchema().build {
      properties -= "age"
    }

    assert(withoutAge.draft7().properties.keys).hasSize(1)
    assert(withoutAge.draft7().properties.keys).contains("name")
    assert(withoutAge.draft7().properties.keys).doesNotContain("age")
  }

  @Test fun testSetters() {
    val mutable = MutableJsonSchema(JsonSchemas.schemaLoader, "http://mverse/mutableSchemas".toURI())

    mutable.maxProperties = 10
    mutable.minProperties = 11
    mutable.requiredProperties = setOf("requiredProperties")
    mutable.exclusiveMinimum = 12
    mutable.exclusiveMaximum = 13
    mutable.propertyNameSchema {
      const = "propertyNames"
    }
    mutable.title = "title"
    mutable.description = "description"
    mutable.types = setOf(JsonSchemaType.STRING)
    mutable.defaultValue = jsrString("default")
    mutable.format = "format"
    mutable.minLength = 14
    mutable.maxLength = 15
    mutable.pattern = "pattern"
    mutable.minimum = 16
    mutable.maximum = 17
    mutable.multipleOf = 18
    mutable.minItems = 19
    mutable.maxItems = 20
    mutable.needsUniqueItems = true
    mutable.comment = "comment"
    mutable.readOnly = true
    mutable.writeOnly = true
    mutable.contentEncoding = "contentEncoding"
    mutable.contentMediaType = "contentMediaType"
    mutable.definitions["definition"] = {
      const = "definition"
    }
    mutable.schemaOfAdditionalItems {
      const = "additionalItems"
    }

    mutable.allItemsSchema { const = "allItems" }

    mutable.additionalProperties = false
    mutable.constValue = jsrString("constance")
    mutable.additionalProperties = false
    mutable.allItemsSchema {
      const = "allItems"
    }
    mutable.allOfSchema {
      const = "allOf"
    }
    mutable.oneOfSchema {
      const = "oneOf"
    }

    mutable.anyOfSchema {
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

      assert(mutable.maxProperties).isEqualTo(10)
      assert(mutable.minProperties).isEqualTo(11)
      assert(mutable.requiredProperties).isEqualTo(setOf("requiredProperties"))
      assert(mutable.exclusiveMinimum).isEqualTo(12)
      assert(mutable.exclusiveMaximum).isEqualTo(13)
      assert(mutable.propertyNameSchema?.const).isEqualTo("propertyNames")
      assert(mutable.title).isEqualTo("title")
      assert(mutable.description).isEqualTo("description")
      assert(mutable.types).isEqualTo(setOf(JsonSchemaType.STRING))
      assert(mutable.defaultValue).isEqualTo(jsrString("default"))
      assert(mutable.format).isEqualTo("format")
      assert(mutable.minLength).isEqualTo(14)
      assert(mutable.maxLength).isEqualTo(15)
      assert(mutable.pattern).isEqualTo("pattern")
      assert(mutable.minimum).isEqualTo(16)
      assert(mutable.maximum).isEqualTo(17)
      assert(mutable.multipleOf).isEqualTo(18)
      assert(mutable.minItems).isEqualTo(19)
      assert(mutable.maxItems).isEqualTo(20)
      assert(mutable.needsUniqueItems).isEqualTo(true)
      assert(mutable.comment).isEqualTo("comment")
      assert(mutable.readOnly).isEqualTo(true)
      assert(mutable.writeOnly).isEqualTo(true)
      assert(mutable.contentEncoding).isEqualTo("contentEncoding")
      assert(mutable.contentMediaType).isEqualTo("contentMediaType")
      assert(mutable.definitions["definition"]?.draft7()?.constValue).isEqualTo(jsrString("definition"))
      assert(mutable.schemaOfAdditionalItems?.const).isEqualTo("additionalItems")
    }
  }
}
