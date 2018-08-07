package io.mverse.jsonschema.loading

import io.mverse.jsonschema.SchemaLocation
import io.mverse.jsonschema.utils.JsonUtils
import kotlinx.serialization.json.JsonElement
import lang.format

data class LoadingIssue(
    val code: String? = null,
    val location: SchemaLocation? = null,
    val schemaJson: JsonElement? = null,
    val value: JsonElement? = null,
    val resolutionMessage: String? = null,
    val level: LoadingIssueLevel? = null,
    val message: String,
    val arguments: List<Any> = emptyList()) {

  override fun toString(): String {
    return listOf(
        level?.name,
        location?.jsonPointerFragment,
        message.format(*JsonUtils.prettyPrintArgs(arguments)))
        .joinToString(",")
  }
}
