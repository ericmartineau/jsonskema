package io.mverse.jsonschema.enums

import lang.Field
import lang.URI

enum class JsonSchemaVersion constructor(uri: String?) {
  Draft3("http://json-schema.org/draft-03/schema#"),
  Draft4("http://json-schema.org/draft-04/schema#"),
  Draft5("http://json-schema.org/draft-05/schema#"),
  Draft6("http://json-schema.org/draft-06/schema#"),
  Draft7("http://json-schema.org/draft-07/schema#"),
  Custom(null),
  Unknown(null);

  private val uri: URI? = if (uri != null) URI(uri) else null

  val metaschemaURI: URI?
    get() = uri

  val isPublic: Boolean
    get() = uri != null

  fun isBefore(otherVersion: JsonSchemaVersion): Boolean {
    return this.compareTo(otherVersion) < 0
  }

  companion object {
    @Field
    val publicVersions = JsonSchemaVersion.values()
        .filter{ v -> v.metaschemaURI != null }

    @Field
    val latest: JsonSchemaVersion = Draft7
  }
}
