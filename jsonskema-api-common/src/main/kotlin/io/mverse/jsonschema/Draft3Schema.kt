package io.mverse.jsonschema

import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.enums.JsonSchemaVersion
import lang.collection.SetMultimap
import lang.json.JsrArray
import lang.json.JsrValue
import lang.net.URI
import kotlin.reflect.KClass

interface Draft3Schema : DraftSchema {

  val isAnyType: Boolean

  val disallow: Set<JsonSchemaType>

  val extendsSchema: DraftSchema?

  val isRequired: Boolean

  // ###################################
  // #### NUMBER KEYWORDS ##############
  // ###################################
  val divisibleBy: Number?

  val isExclusiveMinimum: Boolean?

  val isExclusiveMaximum: Boolean?

  val isAllowAdditionalItems: Boolean

  // ###################################
  // #### OBJECT KEYWORDS  ##############
  // ###################################

  val isAllowAdditionalProperties: Boolean
}
