package io.mverse.jsonschema.assertj.asserts

import assertk.Assert
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.key
import com.google.common.collect.Maps
import io.mverse.jsonschema.AllKeywords
import io.mverse.jsonschema.Draft7Schema
import io.mverse.jsonschema.JsonSchemas
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.enums.JsonSchemaVersion.Draft7
import io.mverse.jsonschema.keyword.Keyword
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.resourceLoader
import io.mverse.jsonschema.validation.ValidationMocks
import lang.json.JsrValue

typealias Draft7Assert = Assert<Draft7Schema>
typealias SchemaAssert = Assert<Schema>

fun AllKeywords.asserting(): SchemaAssert = assertThat(this)
fun AllKeywords.validating(input: JsrValue?): SchemaValidationAssert = assertThat(this).validating(input)

fun Schema.asserting(): SchemaAssert = assertThat(this)
fun Schema.validating(input: JsrValue?): SchemaValidationAssert = assertThat(this).validating(input)

fun Assert<Schema>.isDraft7(): Draft7Assert {
  transform("schema.version") { it.version }
      .isEqualTo(Draft7)

  return transform { it.draft7() }
}

fun SchemaAssert.isVersion(version: JsonSchemaVersion): SchemaAssert {
  transform("schema.version") { it.version }.isEqualTo(version)
  return this
}

fun SchemaAssert.isSchemaEqual(other: Schema) {
  given { actual ->
    val difference = Maps.difference(actual.keywords, other.keywords)
    if (!difference.areEqual()) {
      var msg = ""
      msg += "Schemas don't match: \n"
      if (difference.entriesOnlyOnLeft().isNotEmpty()) {
        msg += "- ONLY ON LEFT: ${difference.entriesOnlyOnLeft().keys} \n"
      }
      if (difference.entriesOnlyOnRight().isNotEmpty()) {
        msg += "- ONLY ON RIGHT: ${difference.entriesOnlyOnRight().keys} \n"
      }

      if (difference.entriesDiffering().isNotEmpty()) {
        msg += "- DIFFERING: \n"
        difference.entriesDiffering().forEach { t, diff ->
          msg += "  - Key: $t\n"
          msg += "      - LEFT: ${diff.leftValue().toString().prependIndent("      ")}"
          msg += "      - RIGHT: ${diff.rightValue().toString().prependIndent("      ")}"
        }
      }
    }
  }
}

inline fun <reified K : Keyword<*>, reified I : KeywordInfo<K>> SchemaAssert.hasKeyword(keyword: I): Assert<K> {
  transform("keywords") { it.keywords }.key(keyword).isNotNull()
  return transform { it.keywords[keyword] as K }
}

fun SchemaAssert.hasProperty(property: String): SchemaAssert {
  transform("schema.properties") { actual -> actual.keywords }
      .key(Keywords.PROPERTIES).isNotNull()

  return transform("schema property $property") { it.draft7().properties[property] }
      .isNotNull()
}

fun SchemaAssert.validating(toValidate: JsrValue?): SchemaValidationAssert {
  return transform { actual ->
    val validator = ValidationMocks.createTestValidator(actual)
    validator.validate(toValidate!!)
  }
}

fun SchemaAssert.validating(resourcePath: String): SchemaValidationAssert {
  val toValidate = JsonSchemas.resourceLoader().readJson(resourcePath)
  return transform { actual ->
    ValidationMocks.createTestValidator(actual).validate(toValidate)
  }
}

