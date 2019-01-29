package io.mverse.jsonschema

interface Draft7Schema : Draft6Schema {
  val ifSchema: Schema?
  val elseSchema: Schema?
  val thenSchema: Schema?
  val comment: String?
  val isReadOnly: Boolean
  val isWriteOnly: Boolean
  val contentEncoding: String?
  val contentMediaType: String?
}
