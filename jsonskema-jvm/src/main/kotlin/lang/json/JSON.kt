package lang.json

import io.mverse.jsonschema.JsonPath
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.KeywordInfoSerializer
import io.mverse.jsonschema.validation.ValidationError
import kotlinx.serialization.context.SimpleModule
import lang.URISerializer
import java.net.URI

val JSON = kotlinx.serialization.json.JSON.nonstrict.apply {
  install(SimpleModule(JsonSchemaType::class, JsonSchemaType.Companion))
  install(SimpleModule(JsonPath::class, JsonPath.serializer()))
  install(SimpleModule(ValidationError::class, ValidationError.serializer()))
  install(SimpleModule(URI::class, URISerializer))
  install(SimpleModule(KeywordInfo::class, KeywordInfoSerializer()))
}