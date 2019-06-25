package io.mverse.jsonschema.utils

import assertk.Assert
import assertk.assert
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.serialization.json.json
import lang.json.jsrObject
import kotlin.test.Test

class JsonUtilsTest {

  @Test
  fun testToPrettyString_Indent() {
    val json = jsrObject {
      "name" *=  "Eric"
      "age" *=  34
      "address" *=  jsrObject {
        "line1" *=  "123 W. East"
        "city" *=  "Gilbert"
        "state" *=  "AZ"
        "postalCode" *=  "85295"
      }
    }
    val indentedValue = json.toString()
    assertThat(indentedValue).isEqualToIgnoringWhitespace("{\n" +
        "\t  \"name\":\"Eric\",\n" +
        "\t  \"age\":34,\n" +
        "\t  \"address\":{\n" +
        "\t    \"line1\":\"123 W. East\",\n" +
        "\t    \"city\":\"Gilbert\",\n" +
        "\t    \"state\":\"AZ\",\n" +
        "\t    \"postalCode\":\"85295\"\n" +
        "\t  }\n" +
        "\t}")
  }

  fun Assert<String>.isEqualToIgnoringWhitespace(other: String?) {
    assert(this.actual.stripWhitespace())
        .isEqualTo(other?.stripWhitespace())
  }

  fun String.stripWhitespace(): String = replace(Regex("\\s"), "")

  @Test
  fun testToPrettyString_NoIndent() {
    val json = jsrObject {
      "name" *= "Eric"
      "age" *= 34
      "address" *= jsrObject {
        "line1" *= "123 W. East"
        "city" *= "Gilbert"
        "state" *= "AZ"
        "postalCode" *= "85295"
      }
    }
    val indentedValue = json.toString()
    assertThat(indentedValue).isEqualToIgnoringWhitespace("{\n" +
        "\t  \"name\":\"Eric\",\n" +
        "\t  \"age\":34,\n" +
        "\t  \"address\":{\n" +
        "\t    \"line1\":\"123 W. East\",\n" +
        "\t    \"city\":\"Gilbert\",\n" +
        "\t    \"state\":\"AZ\",\n" +
        "\t    \"postalCode\":\"85295\"\n" +
        "\t  }\n" +
        "\t}")
  }
}
