package lang.json

import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.KeywordInfoSerializer
import io.mverse.jsonschema.validation.ValidationError
import kotlinx.serialization.context.SimpleModule

val JSON = kotlinx.serialization.json.JSON.nonstrict.apply {
  install(SimpleModule(JsonSchemaType::class, JsonSchemaType.Companion))
  install(SimpleModule(ValidationError::class, ValidationError.serializer()))
  install(SimpleModule(KeywordInfo::class, KeywordInfoSerializer()))
}