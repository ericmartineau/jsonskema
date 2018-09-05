package io.mverse.jsonschema.loading

import io.mverse.jsonschema.SchemaLocation
import io.mverse.jsonschema.utils.JsonUtils
import kotlinx.serialization.json.JsonElement
import lang.Name
import lang.format

data class LoadingIssue(
    @Name("code") val code: String? = null,
    @Name("location") val location: SchemaLocation? = null,
    @Name("schemaJson") val schemaJson: JsonElement? = null,
    @Name("value") val value: JsonElement? = null,
    @Name("resolutionMessage") val resolutionMessage: String? = null,
    @Name("level") val level: LoadingIssueLevel? = null,
    @Name("message") val message: String,
    @Name("arguments") val arguments: List<Any> = emptyList()) {

  override fun toString(): String {
    return listOf(
        level?.name,
        location?.jsonPointerFragment,
        message.format(*JsonUtils.prettyPrintArgs(arguments)))
        .joinToString(",")
  }
}
