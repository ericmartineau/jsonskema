package io.mverse.jsonschema

import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.enums.JsonSchemaVersion
import lang.collection.SetMultimap
import lang.json.JsrArray
import lang.json.JsrValue
import lang.net.URI

interface Draft7Schema : DraftSchema {

  val examples: JsrArray

  val definitions: Map<String, DraftSchema>

  // ###################################
  // #### Draft 7 KEYWORDS #############
  // ###################################

  val ifSchema: DraftSchema?

  val elseSchema: DraftSchema?

  val thenSchema: DraftSchema?

  val comment: String?

  val isReadOnly: Boolean

  val isWriteOnly: Boolean

  val contentEncoding: String?

  val contentMediaType: String?

  val notSchema: DraftSchema?

  val constValue: JsrValue?

  val allOfSchemas: List<DraftSchema>

  val anyOfSchemas: List<DraftSchema>

  val oneOfSchemas: List<DraftSchema>

  val exclusiveMinimum: Number?

  val exclusiveMaximum: Number?

  val containsSchema: DraftSchema?

  val propertyNameSchema: DraftSchema?

  val maxProperties: Int?

  val minProperties: Int?

  val requiredProperties: Set<String>
}
