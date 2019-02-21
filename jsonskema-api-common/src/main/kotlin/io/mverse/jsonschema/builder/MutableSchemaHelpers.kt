package io.mverse.jsonschema.builder

import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.BooleanKeyword
import io.mverse.jsonschema.keyword.JsonArrayKeyword
import io.mverse.jsonschema.keyword.JsonValueKeyword
import io.mverse.jsonschema.keyword.JsrIterable
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.NumberKeyword
import io.mverse.jsonschema.keyword.SchemaListKeyword
import io.mverse.jsonschema.keyword.SingleSchemaKeyword
import io.mverse.jsonschema.keyword.StringKeyword
import io.mverse.jsonschema.keyword.StringSetKeyword
import io.mverse.jsonschema.keyword.URIKeyword
import lang.json.JsrValue
import lang.net.URI

interface MutableSchemaHelpers : MutableSchema {
  operator fun set(keyword: KeywordInfo<URIKeyword>, uri: URI?) {
    this[keyword] = uri?.let { URIKeyword(uri) }
  }

  operator fun set(keyword: KeywordInfo<JsonValueKeyword>, value: JsrValue?) {
    this[keyword] = value?.let { JsonValueKeyword(value) }
  }

  operator fun set(keyword: KeywordInfo<JsonArrayKeyword>, value: JsrIterable?) {
    this[keyword] = value?.let { JsonArrayKeyword(value) }
  }

  operator fun set(keyword: KeywordInfo<BooleanKeyword>, value: Boolean?) {
    this[keyword] = value?.let { BooleanKeyword(value) }
  }

  operator fun set(keyword: KeywordInfo<StringSetKeyword>, value: Set<String>?) {
    this[keyword] = value?.let { StringSetKeyword(value) }
  }

  operator fun set(keyword: KeywordInfo<SchemaListKeyword>, value: List<MutableSchema>?) {
    this[keyword] = value?.let { SchemaListKeyword(buildSubSchemas(value, keyword)) }
  }

  operator fun set(keyword: KeywordInfo<NumberKeyword>, value: Number?) {
    this[keyword] = value?.let { NumberKeyword(value) }
  }

  operator fun set(keyword: KeywordInfo<StringKeyword>, value: String?) {
    this[keyword] = value?.let { StringKeyword(value) }
  }

  operator fun set(keyword: KeywordInfo<SingleSchemaKeyword>, value: MutableSchema?) {
    this[keyword] = value?.let { SingleSchemaKeyword(buildSubSchema(it, keyword)) }
  }

  operator fun set(keyword: KeywordInfo<SingleSchemaKeyword>, property: String, value: MutableSchema?) {
    this[keyword] = value?.let { SingleSchemaKeyword(buildSubSchema(it, keyword, property)) }
  }

  fun buildSubSchema(toBuild: MutableSchema, keyword: KeywordInfo<*>): Schema {
    val childLocation = this.location.child(keyword)
    return toBuild.build(childLocation, loadingReport)
  }

  fun buildSubSchemas(toBuild: Collection<MutableSchema>, keyword: KeywordInfo<*>): List<Schema> {
    var idx = 0
    val childPath = this.location.child(keyword)
    return toBuild.map { builder -> builder.build(childPath.child(idx++), loadingReport) }
  }
}
