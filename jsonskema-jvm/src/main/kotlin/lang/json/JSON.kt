@file:Suppress("EXPERIMENTAL_API_USAGE")

package lang.json

import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.keyword.KeywordInfoSerializer
import io.mverse.jsonschema.validation.ValidationError
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.serializersModule

val JSON = Json {
  forMverse()
  serializersModule(JsonSchemaType.Companion)
  serializersModule(ValidationError.serializer())
  serializersModule(KeywordInfoSerializer())
}