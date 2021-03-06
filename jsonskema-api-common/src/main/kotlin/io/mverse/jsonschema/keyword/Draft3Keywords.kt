package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.enums.JsonSchemaVersion.Draft3
import io.mverse.jsonschema.keyword.Keywords.booleanKeyword
import io.mverse.jsonschema.keyword.Keywords.keyword
import io.mverse.jsonschema.keyword.Keywords.singleSchemaKeyword
import lang.Field
import lang.Global
import lang.json.JsrType

object Draft3Keywords {
  //todo:ericm get docs
  @Field val DIVISIBLE_BY = Keywords.numberKeyword("divisibleBy")
      .expects(JsrType.NUMBER)
      .validates(JsonSchemaType.NUMBER, JsonSchemaType.INTEGER)
      .onlyForVersion(Draft3)
      .build()

  /**
   * From draft-3 schema
   *
   *
   * This attribute indicates if the instance must have a value, and not
   * be undefined.  This is false by default, making the instance
   * optional.
   */
  @Field val REQUIRED_DRAFT3 = booleanKeyword("required")
      .onlyForVersion(Draft3)
      .build()

  /**
   * From draft-03
   *
   *
   * This attribute takes the same values as the "type" attribute, however
   * if the instance matches the type or if this value is an array and the
   * instance matches any type or schema in the array, then this instance
   * is not valid.
   */
  @Global
  val DISALLOW = keyword<TypeKeyword>().key("disallow").expects(JsrType.STRING).onlyForVersion(Draft3)
      .additionalDefinition().expects(JsrType.ARRAY)
      .build()

  /**
   * From draft-03
   *
   *
   * The value of this property MUST be another schema which will provide
   * a base schema which the current schema will inherit from.  The
   * inheritance rules are such that any instance that is valid according
   * to the current schema MUST be valid according to the referenced
   * schema.  This MAY also be an array, in which case, the instance MUST
   * be valid for all the schemas in the array.  A schema that extends
   * another schema MAY define additional attributes, constrain existing
   * attributes, or add other constraints.
   *
   *
   * Conceptually, the behavior of extends can be seen as validating an
   * instance against all constraints in the extending schema as well as
   * the extended schema(s).  More optimized implementations that merge
   * schemas are possible, but are not required.  An example of using
   * "extends":
   *
   *
   * {
   * "description":"An adult",
   * "properties":{"age":{"minimum": 21}},
   * "extends":"person"
   * }
   *
   *
   * {
   * "description":"Extended schema",
   * "properties":{"deprecated":{"type": "boolean"}},
   * "extends":"http://json-schema.org/draft-03/schema"
   * }
   */
  val EXTENDS = singleSchemaKeyword("extends").expects(JsrType.OBJECT).onlyForVersion(Draft3).build()
}
