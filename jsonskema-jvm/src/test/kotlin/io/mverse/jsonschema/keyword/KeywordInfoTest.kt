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
import lang.json.JsrType
import org.junit.Test

class KeywordInfoTest {

  @Test
  fun testBuilder_WhenDeprecatedVersions_AllVersionsWorkProperly() {
    val keyword = KeywordInfo.builder<Keyword<*>>().key("enumeration")
        .expects(JsrType.STRING).since(JsonSchemaVersion.Draft6)
        .additionalDefinition().expects(JsrType.ARRAY).from(JsonSchemaVersion.Draft3).until(Draft5)
        .build()

    assert(keyword.key).isEqualTo("enumeration")
    assert(keyword.variants).hasSize(2)
    assert(keyword.expects).isEqualTo(JsrType.STRING)
    assert(keyword.applicableTypes.size).isGreaterThan(1)

    val draft6 = keyword.getTypeVariant(JsrType.STRING)
    assert(draft6).isNotNull()
    val draft6Keyword = draft6!!
    assert(draft6Keyword.variants).hasSize(0)
    assert(draft6Keyword.applicableVersions).all {
      contains(JsonSchemaVersion.Draft6)
      doesNotContain(Draft5)
    }

    val draft4 = keyword.getTypeVariant(JsrType.ARRAY)
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
    val keyword = KeywordInfo.builder<Keyword<*>>().key("enumeration")
        .expects(JsrType.STRING).validates(JsonSchemaType.INTEGER).since(JsonSchemaVersion.Draft6)
        .additionalDefinition().expects(JsrType.ARRAY).from(JsonSchemaVersion.Draft3).until(Draft5)
        .build()

    assertAll {
      val draft4 = keyword.getTypeVariant(JsrType.STRING)
      assert(draft4).isNotNull {
        val draft4Keyword = it.actual
        assert(draft4Keyword.key).isEqualTo("enumeration")
        assert(draft4Keyword.variants).hasSize(0)
        assert(draft4Keyword.expects)
            .isEqualTo(JsrType.STRING)
        assert(draft4Keyword.applicableTypes).all {
          hasSize(1)
          contains(JsrType.NUMBER)
        }
      }
    }
  }
}
