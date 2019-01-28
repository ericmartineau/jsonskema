package io.mverse.jsonschema

import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.enums.JsonSchemaVersion
import lang.collection.SetMultimap
import lang.json.JsrArray
import lang.json.JsrValue
import lang.net.URI

interface Draft6Schema : DraftSchema {

  // ###################################
  // #### Meta KEYWORDS ##############
  // ###################################

  val examples: JsrArray

  val definitions: Map<String, DraftSchema>

  // ###################################
  // #### Shared KEYWORDS ##############
  // ###################################

  val notSchema: DraftSchema?

  val constValue: JsrValue?

  val allOfSchemas: List<DraftSchema>

  val anyOfSchemas: List<DraftSchema>

  val oneOfSchemas: List<DraftSchema>

  // ###################################
  // #### NUMBER KEYWORDS ##############
  // ###################################

  val exclusiveMinimum: Number?

  val exclusiveMaximum: Number?

  // ###################################
  // #### ARRAY KEYWORDS  ##############
  // ###################################

  val containsSchema: DraftSchema?

  // ###################################
  // #### OBJECT KEYWORDS  ##############
  // ###################################

  val propertyNameSchema: DraftSchema?

  val maxProperties: Int?

  val minProperties: Int?

  val requiredProperties: Set<String>

}
