package io.mverse.jsonschema.builder

import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.keyword.KeywordInfo

fun MutableSchema.buildSubSchema(toBuild: MutableSchema, keyword: KeywordInfo<*>): Schema {
  val childLocation = this.location.child(keyword)
  return toBuild.build(childLocation, loadingReport)
}

fun MutableSchema.buildSubSchemas(toBuild: Collection<MutableSchema>, keyword: KeywordInfo<*>): List<Schema> {
  var idx = 0
  val childPath = this.location.child(keyword)
  return toBuild.map { builder -> builder.build(childPath.child(idx++), loadingReport) }
}


fun MutableSchema.buildSubSchema(toBuild: MutableSchema, keyword: KeywordInfo<*>, path: String, vararg paths: String): Schema {
  val childLocation = this.location.child(keyword).child(path).child(*paths)
  return toBuild.build(childLocation, loadingReport)
}

