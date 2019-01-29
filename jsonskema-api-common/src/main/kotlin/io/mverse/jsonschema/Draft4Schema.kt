package io.mverse.jsonschema

interface Draft4Schema : DraftSharedKeywords {
  val notSchema: Schema?
  val allOfSchemas: List<Schema>
  val anyOfSchemas: List<Schema>
  val oneOfSchemas: List<Schema>
  val isExclusiveMinimum: Boolean?
  val isExclusiveMaximum: Boolean?
  val maxProperties: Int?
  val minProperties: Int?
  val requiredProperties: Set<String>
  val exclusiveMinimum: Number?
  val exclusiveMaximum: Number?
}
