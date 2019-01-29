package io.mverse.jsonschema

import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.keyword.Keywords

interface Draft3Schema : DraftSharedKeywords {
  val isAnyType: Boolean
  val disallow: Set<JsonSchemaType>
  val extendsSchema: Schema?
  val isRequired: Boolean
  val divisibleBy: Number?
  val isExclusiveMinimum: Boolean?
  val isExclusiveMaximum: Boolean?
}

fun main() {
  Keywords.all.groupBy {
    it.applicableVersions.first()
  }.forEach {
    println("${it.key}:")
    it.value.forEach {
      println("\t- ${it.key}")
    }
  }

}
