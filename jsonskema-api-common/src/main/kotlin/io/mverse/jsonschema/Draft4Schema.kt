package io.mverse.jsonschema

import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.enums.JsonSchemaVersion
import lang.collection.SetMultimap
import lang.json.JsrArray
import lang.json.JsrValue
import lang.net.URI

interface Draft4Schema : DraftSchema {

  val notSchema: DraftSchema?

  val allOfSchemas: List<DraftSchema>

  val anyOfSchemas: List<DraftSchema>

  val oneOfSchemas: List<DraftSchema>

  val isExclusiveMinimum: Boolean?

  val isExclusiveMaximum: Boolean?

  val isAllowAdditionalItems: Boolean

  // ###################################
  // #### OBJECT KEYWORDS  ##############
  // ###################################

  val isAllowAdditionalProperties: Boolean

  val maxProperties: Int?

  val minProperties: Int?

  val requiredProperties: Set<String>

}
