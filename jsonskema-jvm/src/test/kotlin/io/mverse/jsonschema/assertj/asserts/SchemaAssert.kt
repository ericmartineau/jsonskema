package io.mverse.jsonschema.assertj.asserts

import assertk.Assert
import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.key
import io.mverse.jsonschema.Draft7Schema
import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.ValidationMocks
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.enums.JsonSchemaVersion.Draft7
import io.mverse.jsonschema.keyword.JsonSchemaKeyword
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.resourceLoader
import kotlinx.serialization.json.JsonElement

typealias Draft7Assert = Assert<Draft7Schema>
typealias SchemaAssert = Assert<Schema>

fun Schema.asserting(): SchemaAssert = assert(this)
fun Schema.validating(input:JsonElement): SchemaValidationAssert = assert(this).validating(input)

fun Assert<Schema>.isDraft7(): Draft7Assert {
  assert(actual.version, "schema.version")
      .isEqualTo(Draft7)

  return assert(actual.asDraft7())
}

fun SchemaAssert.isVersion(version: JsonSchemaVersion): SchemaAssert {
  assert(actual.version, "schema.version").isEqualTo(version)
  return this
}

inline fun <reified K : JsonSchemaKeyword<*>, reified I : KeywordInfo<K>> SchemaAssert.hasKeyword(keyword: I): Assert<K> {
  assert(actual.keywords, "schema.keywords")
      .key(keyword) { k ->
        k.isNotNull()
      }
  val v = actual.keywords[keyword] as K
  return assert(v)
}

fun SchemaAssert.hasProperty(property: String): Draft7Assert {
  assert(actual.keywords, "schema.properties")
      .key(Keywords.PROPERTIES) {
        it.isNotNull()
      }

  val propSchema = actual.asDraft7().findPropertySchema(property)
  assert(propSchema, "contains $property").isNotNull()
  return assert(propSchema!!)
}

fun SchemaAssert.validating(toValidate: JsonElement): SchemaValidationAssert {
  val result = ValidationMocks.createTestValidator(actual).validate(toValidate)
  return assert(result)
}

fun SchemaAssert.validating(resourcePath: String): SchemaValidationAssert {
  val toValidate = JsonSchema.resourceLoader().readJson(resourcePath)
  val result = ValidationMocks.createTestValidator(actual).validate(toValidate)
  return assert(result)
}

