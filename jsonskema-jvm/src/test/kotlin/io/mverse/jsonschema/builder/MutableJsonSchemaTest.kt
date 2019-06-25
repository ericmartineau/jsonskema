package io.mverse.jsonschema.builder

import assertk.assert
import assertk.assertAll
import assertk.assertThat
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

    assertThat(mutable.const).isEqualTo("yes")
    assertThat(mutable.constValue).isEqualTo(jsrString("yes"))
  }

  @Test fun testSetters_WithoutUpdater() {
    val mutable = MutableJsonSchema(JsonSchemas.schemaLoader, "http://mverse/mutableSchemas".toURI())
    mutable[Keywords.CONST] = JsonValueKeyword("yes")

    assertThat(mutable.const).isEqualTo("yes")
    assertThat(mutable.constValue).isEqualTo(jsrString("yes"))
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

    assertThat(withoutAge.draft7().properties.keys).hasSize(1)
    assertThat(withoutAge.draft7().properties.keys).contains("name")
    assertThat(withoutAge.draft7().properties.keys).doesNotContain("age")
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
      assertThat(mutable.enumValues?.firstOrNull()).isEqualTo(jsrNumber(0))
      assertThat(mutable.enumValues?.firstOrNull()).isEqualTo(jsrNumber(1))
      assertThat(mutable.enumValues?.firstOrNull()).isEqualTo(jsrNumber(2))

      assertThat(mutable.anyOfSchemas.first().const).isEqualTo("anyOf")
      assertThat(mutable.oneOfSchemas.first().const).isEqualTo("oneOf")
      assertThat(mutable.allOfSchemas.first().const).isEqualTo("allOf")
      assertThat(mutable.itemSchemas.first().const).isEqualTo("items")
      assertThat(mutable.allItemSchema?.const).isEqualTo("allItems")
      assertThat(mutable.properties["property"].draft7().constValue).isEqualTo(jsrString("property"))

      assertThat(mutable.maxProperties).isEqualTo(10)
      assertThat(mutable.minProperties).isEqualTo(11)
      assertThat(mutable.requiredProperties).isEqualTo(setOf("requiredProperties"))
      assertThat(mutable.exclusiveMinimum).isEqualTo(12)
      assertThat(mutable.exclusiveMaximum).isEqualTo(13)
      assertThat(mutable.propertyNameSchema?.const).isEqualTo("propertyNames")
      assertThat(mutable.title).isEqualTo("title")
      assertThat(mutable.description).isEqualTo("description")
      assertThat(mutable.types).isEqualTo(setOf(JsonSchemaType.STRING))
      assertThat(mutable.defaultValue).isEqualTo(jsrString("default"))
      assertThat(mutable.format).isEqualTo("format")
      assertThat(mutable.minLength).isEqualTo(14)
      assertThat(mutable.maxLength).isEqualTo(15)
      assertThat(mutable.pattern).isEqualTo("pattern")
      assertThat(mutable.minimum).isEqualTo(16)
      assertThat(mutable.maximum).isEqualTo(17)
      assertThat(mutable.multipleOf).isEqualTo(18)
      assertThat(mutable.minItems).isEqualTo(19)
      assertThat(mutable.maxItems).isEqualTo(20)
      assertThat(mutable.needsUniqueItems).isEqualTo(true)
      assertThat(mutable.comment).isEqualTo("comment")
      assertThat(mutable.readOnly).isEqualTo(true)
      assertThat(mutable.writeOnly).isEqualTo(true)
      assertThat(mutable.contentEncoding).isEqualTo("contentEncoding")
      assertThat(mutable.contentMediaType).isEqualTo("contentMediaType")
      assertThat(mutable.definitions["definition"]?.draft7()?.constValue).isEqualTo(jsrString("definition"))
      assertThat(mutable.schemaOfAdditionalItems?.const).isEqualTo("additionalItems")
    }
  }
}
