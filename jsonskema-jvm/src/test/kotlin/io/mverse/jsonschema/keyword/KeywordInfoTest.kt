package io.mverse.jsonschema.keyword

import assertk.all
import assertk.assert
import assertk.assertAll
import assertk.assertions.contains
import assertk.assertions.doesNotContain
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import assertk.assertions.isNotNull
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.enums.JsonSchemaVersion.Draft5
import lang.json.ValueType

class KeywordInfoTest {

  @kotlin.test.Test
  fun testBuilder_WhenDeprecatedVersions_AllVersionsWorkProperly() {
    val keyword = KeywordInfo.builder<JsonSchemaKeyword<*>>().key("enumeration")
        .expects(ValueType.STRING).since(JsonSchemaVersion.Draft6)
        .additionalDefinition().expects(ValueType.ARRAY).from(JsonSchemaVersion.Draft3).until(Draft5)
        .build()

    assert(keyword.key).isEqualTo("enumeration")
    assert(keyword.variants).hasSize(2)
    assert(keyword.expects).isEqualTo(ValueType.STRING)
    assert(keyword.applicableTypes.size).isGreaterThan(1)

    val draft6 = keyword.getTypeVariant(ValueType.STRING)
    assert(draft6).isNotNull()
    val draft6Keyword = draft6!!
    assert(draft6Keyword.variants).hasSize(0)
    assert(draft6Keyword.applicableVersions).all {
      contains(JsonSchemaVersion.Draft6)
      doesNotContain(Draft5)
    }

    val draft4 = keyword.getTypeVariant(ValueType.ARRAY)
    assert(draft4).isNotNull {
      val draft4Keyword = it.actual
      assert(draft4Keyword.key).isEqualTo("enumeration")
      assert(draft4Keyword.variants).hasSize(0)
      assert(draft4Keyword.applicableVersions).all {
        doesNotContain(JsonSchemaVersion.Draft6)
        contains(Draft5)
      }
    }
  }

  @kotlin.test.Test
  fun testBuilder_WhenDeprecatedVersions_DefaultsAreCopiedVersionsWorkProperly() {
    val keyword = KeywordInfo.builder<JsonSchemaKeyword<*>>().key("enumeration")
        .expects(ValueType.STRING).validates(JsonSchemaType.INTEGER).since(JsonSchemaVersion.Draft6)
        .additionalDefinition().expects(ValueType.ARRAY).from(JsonSchemaVersion.Draft3).until(Draft5)
        .build()

    assertAll {
      val draft4 = keyword.getTypeVariant(ValueType.STRING)
      assert(draft4).isNotNull {
        val draft4Keyword = it.actual
        assert(draft4Keyword.key).isEqualTo("enumeration")
        assert(draft4Keyword.variants).hasSize(0)
        assert(draft4Keyword.expects)
            .isEqualTo(ValueType.STRING)
        assert(draft4Keyword.applicableTypes).all {
          hasSize(1)
          contains(ValueType.NUMBER)
        }
      }
    }
  }
}
