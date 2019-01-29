package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.enums.JsonSchemaType.INTEGER
import io.mverse.jsonschema.enums.JsonSchemaVersion.Draft3
import io.mverse.jsonschema.enums.JsonSchemaVersion.Draft4
import io.mverse.jsonschema.enums.JsonSchemaVersion.Draft5
import io.mverse.jsonschema.enums.JsonSchemaVersion.Draft6
import io.mverse.jsonschema.enums.JsonSchemaVersion.Draft7
import io.mverse.jsonschema.keyword.KeywordInfo.KeywordInfoBuilder

import lang.Field
import lang.Global
import lang.collection.freezeList
import lang.json.JsrType

object Keywords {

  @Field val DOLLAR_ID_KEY = "\$id"
  @Field val ID_KEY = "id"

  internal val keywordCollector = mutableListOf<KeywordInfo<*>>()

  @Field
  val SCHEMA = keyword<DollarSchemaKeyword>().key("\$schema").expects(JsrType.STRING).build()

  /**
   * From draft-06
   *
   *
   * The "$ref" keyword is used to reference a schema, and provides the ability to
   * validate recursive structures through self-reference.
   *
   *
   * An object schema with a "$ref" property MUST be interpreted as a "$ref" reference.
   * The value of the "$ref" property MUST be a URI Reference. Resolved against the current
   * URI base, it identifies the URI of a schema to use. All other properties in a "$ref"
   * object MUST be ignored.
   *
   *
   * The URI is not a network locator, only an identifier. A schema need not be downloadable
   * from the address if it is a network-addressable URL, and implementations SHOULD NOT assume
   * they should perform a network operation when they encounter a network-addressable URI.
   *
   *
   * A schema MUST NOT be run into an infinite loop against a schema. For example, if two
   * schemas "#alice" and "#bob" both have an "allOf" property that refers to the other, a naive
   * validation might get stuck in an infinite recursive loop trying to validate the instance. Schemas
   * SHOULD NOT make use of infinite recursive nesting like this; the behavior is undefined.
   */
  @Field val REF = uriKeyword("\$ref").expects(JsrType.STRING).build()

  /**
   * From draft-06
   *
   *
   * The "$id" keyword defines a URI for the schema, and the base URI that other URI references
   * within the schema are resolved against. The "$id" keyword itself is resolved against the base
   * URI that the object as a whole appears in.
   *
   *
   * If present, the value for this keyword MUST be a string, and MUST represent a valid URI-reference
   * [RFC3986]. This value SHOULD be normalized, and SHOULD NOT be an empty fragment <#> or an empty string <>.
   *
   *
   * The root schema of a JSON Schema document SHOULD contain an "$id" keyword with a URI (containing
   * a scheme). This URI SHOULD either not have a fragment, or have one that is an empty string. [CREF2]
   *
   *
   * To name subschemas in a JSON Schema document, subschemas can use "$id" to give themselves a
   * document-local identifier. This is done by setting "$id" to a URI reference consisting only of
   * KeywordMetadata * a fragment. The fragment identifier MUST begin with a letter  = [A-Za-z].build(); followed by any number of
   * KeywordMetadata * letters, digits  = [0-9].build(); hyphens ("-"), underscores ("_"), colons (":"), or periods (".").
   *
   *
   * The effect of defining an "$id" that neither matches the above requirements nor is a valid JSON pointer
   * is not defined.
   */
  @Field val DOLLAR_ID = keyword<IdKeyword>().key("\$id")
      .expects(JsrType.STRING)
      .since(Draft3)
      .build()

  /**
   * This keyword is reserved for comments from schema authors to readers or maintainers of the
   * schema. The value of this keyword MUST be a string. Implementations MUST NOT present this
   * string to end users. Tools for editing schemas SHOULD support displaying and editing this keyword.
   *
   * The value of this keyword MAY be used in debug or error output which is intended for developers
   * making use of schemas. Schema vocabularies SHOULD allow "$comment" within any object containing
   * vocabulary keywords. Implementations MAY assume "$comment" is allowed unless the vocabulary
   * specifically forbids it. Vocabularies MUST NOT specify any effect of "$comment" beyond what is
   * described in this specification. Tools that translate other media types or programming languages
   * to and from application/schema+json MAY choose to convert that media type or programming language's
   * native comments to or from "$comment" values. The behavior of such translation when both native
   * comments and "$comment" properties are present is implementation-dependent. Implementations
   * SHOULD treat "$comment" identically to an unknown extension keyword. They MAY strip "$comment"
   * values at any point during processing. In particular, this allows for shortening schemas when
   * the size of deployed schemas is a concern. Implementations MUST NOT take any other action based
   * on the presence, absence, or contents of "$comment" properties.
   */
  @Field val COMMENT = stringKeyword("\$comment").expects(JsrType.STRING)
      .since(Draft7)
      .build()

  /**
   * From draft-03
   *
   *
   * This attribute defines the current URI of this schema (this attribute
   * is effectively a "self" link).  This URI MAY be relative or absolute.
   * If the URI is relative it is resolved against the current URI of the
   * parent schema it is contained in.  If this schema is not contained in
   * any parent schema, the current URI of the parent schema is held to be
   * the URI under which this schema was addressed.  If id is missing, the
   * current URI of a schema is defined to be that of the parent schema.
   * The current URI of the schema is also used to construct relative
   * references such as for $ref.
   */
  @Field val ID = keyword<IdKeyword>().key("id").expects(JsrType.STRING).since(Draft3)
      .until(Draft4).build()

  /**
   * From draft-06
   *
   *
   * The value of both of these keywords MUST be a string.
   *
   *
   * Both of these keywords can be used to decorate a user interface with information about the data
   * produced by this user interface. A title will preferably be short, whereas a description will
   * provide explanation about the purpose of the instance described by this schema.
   */
  @Field val TITLE = stringKeyword("title").expects(JsrType.STRING).build()

  /**
   * The value of these keywords MUST be a boolean. When multiple occurrences of these keywords are
   * applicable to a single sub-instance, the resulting value MUST be true if any occurrence specifies
   * a true value, and MUST be false otherwise.
   *
   * If "readOnly" has a value of boolean true, it indicates that the value of the instance is
   * managed exclusively by the owning authority, and attempts by an application to modify the value
   * of this property are expected to be ignored or rejected by that owning authority.
   *
   * An instance document that is marked as "readOnly for the entire document MAY be ignored if
   * sent to the owning authority, or MAY result in an error, at the authority's discretion.
   *
   * If "writeOnly" has a value of boolean true, it indicates that the value is never present when
   * the instance is retrieved from the owning authority. It can be present when sent to the owning
   * authority to update or create the document (or the resource it represents), but it will not be
   * included in any updated or newly created version of the instance.
   */
  @Field val READ_ONLY = booleanKeyword("readOnly")
      .since(Draft7)
      .build()

  /**
   * The value of these keywords MUST be a boolean. When multiple occurrences of these keywords are
   * applicable to a single sub-instance, the resulting value MUST be true if any occurrence specifies
   * a true value, and MUST be false otherwise.
   *
   * If "readOnly" has a value of boolean true, it indicates that the value of the instance is
   * managed exclusively by the owning authority, and attempts by an application to modify the value
   * of this property are expected to be ignored or rejected by that owning authority.
   *
   * An instance document that is marked as "readOnly for the entire document MAY be ignored if
   * sent to the owning authority, or MAY result in an error, at the authority's discretion.
   *
   * If "writeOnly" has a value of boolean true, it indicates that the value is never present when
   * the instance is retrieved from the owning authority. It can be present when sent to the owning
   * authority to update or create the document (or the resource it represents), but it will not be
   * included in any updated or newly created version of the instance.
   */
  @Field val WRITE_ONLY = booleanKeyword("writeOnly")
      .since(Draft7)
      .build()

  /**
   * 8.3. contentEncoding
   * If the instance value is a string, this property defines that the string SHOULD be interpreted
   * as binary data and decoded using the encoding named by this property. RFC 2045, Sec 6.1 [RFC2045]
   * lists the possible values for this property.
   *
   * The value of this property MUST be a string.
   *
   * The value of this property SHOULD be ignored if the instance described is not a string.
   */
  @Field val CONTENT_ENCODING = stringKeyword("contentEncoding")
      .validates(JsonSchemaType.STRING)
      .since(Draft7)
      .build()

  /**
   * 8.4. contentMediaType
   * The value of this property must be a media type, as defined by RFC 2046 [RFC2046]. This property defines the media type of instances which this schema defines.
   *
   * The value of this property MUST be a string.
   *
   * The value of this property SHOULD be ignored if the instance described is not a string.
   *
   * If the "contentEncoding" property is not present, but the instance value is a string, then the
   * value of this property SHOULD specify a text document type, and the character set SHOULD be the
   * character set into which the JSON string value was decoded (for which the default is Unicode).
   */
  @Field val CONTENT_MEDIA_TYPE = stringKeyword("contentMediaType")
      .validates(JsonSchemaType.STRING)
      .since(Draft7)
      .build()

  /**
   * From draft-06
   *
   *
   * This keyword's value MUST be an object. Each member value of this object MUST be a valid JSON Schema.
   *
   *
   * This keyword plays no role in validation per se. Its role is to provide a standardized location for
   * schema authors to inline JSON Schemas into a more general schema.
   *
   *
   *
   *
   * {
   * "type": "array",
   * "items": { "$ref": "#/definitions/positiveInteger" },
   * "definitions": {
   * "positiveInteger": {
   * "type": "integer",
   * "exclusiveMinimum": 0
   * }
   * }
   * }
   *
   *
   *
   *
   * As an example, here is a schema describing an array of positive integers, where the positive integer
   * constraint is a subschema in "definitions":
   */
  @Field val DEFINITIONS = schemaMapKeyword("definitions")
      .expects(JsrType.OBJECT).since(Draft6).build()
  /**
   * From draft-06
   *
   *
   * The value of both of these keywords (title, description) MUST be a string.
   *
   *
   * Both of these keywords can be used to decorate a user interface with information about the data
   * produced by this user interface. A title will preferably be short, whereas a description will
   * provide explanation about the purpose of the instance described by this schema.
   */
  @Field val DESCRIPTION = stringKeyword("description").expects(JsrType.STRING).build()
  /**
   * From draft-06
   *
   *
   * There are no restrictions placed on the value of this keyword.
   *
   *
   * This keyword can be used to supply a default JSON value associated with a
   * particular schema. It is RECOMMENDED that a default value be valid against
   * the associated schema.
   */
  @Field val DEFAULT = jsonValueKeyword("default").expects(JsrType.ARRAY)
      .additionalDefinition().expects(JsrType.OBJECT)
      .additionalDefinition().expects(JsrType.NUMBER)
      .additionalDefinition().expects(JsrType.BOOLEAN)
      .additionalDefinition().expects(JsrType.STRING)
      .additionalDefinition().expects(JsrType.NULL)
      .build()
  /**
   * The value of "properties" MUST be an object.
   * Each value of this object MUST be a valid JSON Schema.
   *
   *
   * This keyword determines how child instances validate for objects,
   * and does not directly validate the immediate instance itself.
   *
   *
   * Validation succeeds if, for each name that appears in both
   * the instance and as a name within this keyword's value, the child
   * instance for that name successfully validates against the
   * corresponding schema.
   *
   *
   * Omitting this keyword has the same behavior as an empty object.
   */
  @Global
  val PROPERTIES = schemaMapKeyword("properties").expects(JsrType.OBJECT).validates(JsonSchemaType.OBJECT).build()
  /**
   * The value of this keyword MUST be a non-negative integer.
   *
   *
   * An object instance is valid against "maxProperties" if its
   * number of properties is less than, or equal to, the value of this
   * keyword.
   */
  @Global
  val MAX_PROPERTIES = numberKeyword("maxProperties").expects(JsrType.NUMBER).validates(JsonSchemaType.OBJECT).since(Draft4).build()
  /**
   * The value of this keyword MUST be an array.
   * Elements of this array, if any, MUST be strings, and MUST be unique.
   *
   *
   * An object instance is valid against this keyword if every item in the array is
   * the name of a property in the instance.
   *
   *
   * Omitting this keyword has the same behavior as an empty array.
   */
  @Global
  val REQUIRED = stringSetKeyword("required").expects(JsrType.ARRAY).validates(JsonSchemaType.OBJECT).since(Draft4).build()
  /**
   * The value of "additionalProperties" MUST be a valid JSON Schema.
   *
   *
   * This keyword determines how child instances validate for objects,
   * and does not directly validate the immediate instance itself.
   *
   *
   * Validation with "additionalProperties" applies only to the child
   * values of instance names that do not match any names in "properties",
   * and do not match any regular expression in "patternProperties".
   *
   *
   * For all such properties, validation succeeds if the child instance
   * validates against the "additionalProperties" schema.
   *
   *
   * Omitting this keyword has the same behavior as an empty schema.
   */
  @Field val ADDITIONAL_PROPERTIES = singleSchemaKeyword("additionalProperties")
      .expects(JsrType.OBJECT).validates(JsonSchemaType.OBJECT).since(Draft3)
      .additionalDefinition().expects(JsrType.BOOLEAN).from(Draft3).until(Draft5)
      .build()

  /**
   * The value of this keyword MUST be a non-negative integer.
   *
   *
   * An object instance is valid against "minProperties" if its
   * number of properties is greater than, or equal to, the value of this
   * keyword.
   *
   *
   * Omitting this keyword has the same behavior as a value of 0.
   */
  @Field val MIN_PROPERTIES = numberKeyword("minProperties")
      .expects(JsrType.NUMBER)
      .validates(JsonSchemaType.OBJECT)
      .since(Draft4).build()
  /**
   * This keyword specifies rules that are evaluated if the instance is an object and
   * contains a certain property.
   *
   *
   * This keyword's value MUST be an object. Each property specifies a dependency.
   * Each dependency value MUST be an array or a valid JSON Schema.
   *
   *
   * If the dependency value is a subschema, and the dependency key is a property
   * in the instance, the entire instance must validate against the dependency value.
   *
   *
   * If the dependency value is an array, each element in the array,
   * if any, MUST be a string, and MUST be unique. If the dependency key is
   * a property in the instance, each of the items in the dependency
   * value must be a property that exists in the instance.
   *
   *
   * Omitting this keyword has the same behavior as an empty object.
   */
  @Field val DEPENDENCIES: KeywordInfo<DependenciesKeyword> = keyword<DependenciesKeyword>()
      .key("dependencies")
      .expects(JsrType.OBJECT)
      .validates(JsonSchemaType.OBJECT).build()

  /**
   * The value of "patternProperties" MUST be an object. Each property name
   * of this object SHOULD be a valid regular expression, according to the
   * ECMA 262 regular expression dialect. Each property value of this object
   * MUST be a valid JSON Schema.
   *
   *
   * This keyword determines how child instances validate for objects,
   * and does not directly validate the immediate instance itself.
   * Validation of the primitive instance type against this keyword
   * always succeeds.
   *
   *
   * Validation succeeds if, for each instance name that matches any
   * regular expressions that appear as a property name in this keyword's value,
   * the child instance for that name successfully validates against each
   * schema that corresponds to a matching regular expression.
   *
   *
   * Omitting this keyword has the same behavior as an empty object.
   */
  @Field val PATTERN_PROPERTIES = schemaMapKeyword("patternProperties")
      .expects(JsrType.OBJECT).validates(JsonSchemaType.OBJECT).build()

  /**
   * The value of "propertyNames" MUST be a valid JSON Schema.
   *
   *
   * If the instance is an object, this keyword validates if every property name in
   * the instance validates against the provided schema.
   * Note the property name that the schema is testing will always be a string.
   *
   *
   * Omitting this keyword has the same behavior as an empty schema.
   */
  @Field val PROPERTY_NAMES = singleSchemaKeyword("propertyNames")
      .expects(JsrType.OBJECT).validates(JsonSchemaType.OBJECT).since(Draft6).build()

  /**
   * The value of this keyword MUST be either a string or an array. If it is
   * an array, elements of the array MUST be strings and MUST be unique.
   *
   *
   * String values MUST be one of the six primitive types
   * ,
   * or "integer" which matches any number with a zero fractional part.
   *
   *
   * An instance validates if and only if the instance is in any of the sets listed
   * for this keyword.
   */
  //todo:ericm Handle "any" and "disallow"
  @Field val TYPE = keyword<TypeKeyword>()
      .key("type")
      .expects(JsrType.STRING)
      .additionalDefinition().expects(JsrType.ARRAY)
      .build()

  /**
   * The value of "multipleOf" MUST be a number, strictly greater than 0.
   *
   *
   * A numeric instance is valid only if division by this keyword's value results in
   * an integer.
   */
  @Field val MULTIPLE_OF = numberKeyword("multipleOf")
      .expects(JsrType.NUMBER)
      .validates(JsonSchemaType.NUMBER, INTEGER)
      .since(Draft4).build()

  /**
   * The value of "maximum" MUST be a number, representing an inclusive upper limit
   * for a numeric instance.
   *
   *
   * If the instance is a number, then this keyword validates only if the instance is
   * less than or exactly equal to "maximum".
   */
  @Global
  val MAXIMUM = keyword<LimitKeyword>().key("maximum").expects(JsrType.NUMBER).validates(JsonSchemaType.NUMBER, JsonSchemaType.INTEGER).build()

  /**
   * The value of "exclusiveMaximum" MUST be number, representing an exclusive upper
   * limit for a numeric instance.
   *
   *
   * If the instance is a number, then the instance is valid only if it has a value
   * strictly less than (not equal to) "exclusiveMaximum".
   */
  @Field val EXCLUSIVE_MAXIMUM = keyword<LimitKeyword>().key("exclusiveMaximum")
      .expects(JsrType.NUMBER).validates(JsonSchemaType.NUMBER, INTEGER).since(Draft6)
      .additionalDefinition().expects(JsrType.BOOLEAN).from(Draft3).until(Draft5)
      .build()

  /**
   * The value of "minimum" MUST be a number, representing an inclusive lower limit
   * for a numeric instance.
   *
   *
   * If the instance is a number, then this keyword validates only if the instance is
   * greater than or exactly equal to "minimum".
   */
  @Global
  val MINIMUM = keyword<LimitKeyword>().key("minimum").validates(JsonSchemaType.NUMBER, INTEGER).expects(JsrType.NUMBER).build()

  /**
   * The value of "exclusiveMinimum" MUST be number, representing an exclusive lower
   * limit for a numeric instance.
   *
   *
   * If the instance is a number, then the instance is valid only if it has a value
   * strictly greater than (not equal to) "exclusiveMinimum".
   */
  @Global
  val EXCLUSIVE_MINIMUM = keyword<LimitKeyword>().key("exclusiveMinimum").expects(JsrType.NUMBER).validates(JsonSchemaType.NUMBER, JsonSchemaType.INTEGER).since(Draft6)
      .additionalDefinition().expects(JsrType.BOOLEAN).from(Draft3).until(Draft5)
      .build()

  /**
   * Structural validation alone may be insufficient to validate that an instance
   * meets all the requirements of an application. The "format" keyword is defined to
   * allow interoperable semantic validation for a fixed subset of values which are
   * accurately described by authoritative resources, be they RFCs or other external
   * specifications.
   * a href
   *
   *
   * The value of this keyword is called a format attribute. It MUST be a string. A
   * format attribute can generally only validate a given set of instance types. If
   * the type of the instance to validate is not in this set, validation for this
   * format attribute and instance SHOULD succeed.
   * a href
   */
  @Global
  val FORMAT = stringKeyword("format").expects(JsrType.STRING).validates(JsonSchemaType.STRING).build()

  /**
   * The value of this keyword MUST be a non-negative integer.
   * A string instance is valid against this keyword if its
   * length is less than, or equal to, the value of this keyword.
   *
   *
   * The length of a string instance is defined as the number of its
   * characters as defined by [RFC 7159](https://tools.ietf.org/html/RFC7159).
   */
  @Global
  val MAX_LENGTH = numberKeyword("maxLength").expects(JsrType.NUMBER).validates(JsonSchemaType.STRING).build()

  /**
   * The value of this keyword MUST be a non-negative integer.
   *
   *
   * A string instance is valid against this keyword if its
   * length is greater than, or equal to, the value of this keyword.
   * a href
   *
   *
   * The length of a string instance is defined as the number of its
   * characters as defined by [RFC 7159](https://tools.ietf.org/html/RFC7159).
   *
   *
   * Omitting this keyword has the same behavior as a value of 0.
   */
  @Global
  val MIN_LENGTH = numberKeyword("minLength").expects(JsrType.NUMBER).validates(JsonSchemaType.STRING).build()

  /**
   * The value of this keyword MUST be a string. This string SHOULD be a
   * valid regular expression, according to the ECMA 262 regular expression
   * dialect.
   *
   *
   * A string instance is considered valid if the regular
   * expression matches the instance successfully. Recall: regular
   * expressions are not implicitly anchored.
   */
  @Global
  val PATTERN = stringKeyword("pattern").expects(JsrType.STRING).validates(JsonSchemaType.STRING).build()

  /**
   * The value of "items" MUST be either a valid JSON Schema or an array of valid
   * JSON Schemas.
   *
   *
   * This keyword determines how child instances validate for arrays,
   * and does not directly validate the immediate instance itself.
   *
   *
   * If "items" is a schema, validation succeeds if all elements
   * in the array successfully validate against that schema.
   *
   *
   * If "items" is an array of schemas, validation succeeds if
   * each element of the instance validates against the schema at the
   * same position, if any.
   *
   *
   * Omitting this keyword has the same behavior as an empty schema.
   */
  @Global
  val ITEMS = keyword<ItemsKeyword>().key("items").expects(JsrType.ARRAY).validates(JsonSchemaType.ARRAY)
      .additionalDefinition().expects(JsrType.OBJECT)
      .build()

  /**
   * The value of "additionalItems" MUST be a valid JSON Schema.
   *
   *
   * This keyword determines how child instances validate for arrays,
   * and does not directly validate the immediate instance itself.
   *
   *
   * If "items" is an array of schemas, validation succeeds
   * if every instance element at a position greater than the size
   * of "items" validates against "additionalItems".
   *
   *
   * Otherwise, "additionalItems" MUST be ignored, as the "items"
   * schema (possibly the default value of an empty schema) is
   * applied to all elements.
   *
   *
   * Omitting this keyword has the same behavior as an empty schema.
   */
  @Global
  val ADDITIONAL_ITEMS = keyword<ItemsKeyword>().key("additionalItems").expects(JsrType.OBJECT).validates(JsonSchemaType.ARRAY).since(Draft3)
      .additionalDefinition().expects(JsrType.BOOLEAN).validates(JsonSchemaType.ARRAY).from(Draft3).until(Draft5)
      .build()

  /**
   * The value of this keyword MUST be a non-negative integer.
   *
   *
   * An array instance is valid against "maxItems" if its size is
   * less than, or equal to, the value of this keyword.
   */
  @Field val MAX_ITEMS = numberKeyword("maxItems")
      .expects(JsrType.NUMBER).validates(JsonSchemaType.ARRAY).build()

  /**
   * The value of this keyword MUST be a non-negative integer.
   *
   *
   * An array instance is valid against "minItems" if its size is
   * greater than, or equal to, the value of this keyword.
   *
   *
   * Omitting this keyword has the same behavior as a value of 0.
   */
  @Field val MIN_ITEMS = numberKeyword("minItems").expects(JsrType.NUMBER)
      .validates(JsonSchemaType.ARRAY).build()

  /**
   * The value of this keyword MUST be a boolean.
   *
   *
   * If this keyword has boolean value false, the instance validates
   * successfully. If it has boolean value true, the instance validates
   * successfully if all of its elements are unique.
   *
   *
   * Omitting this keyword has the same behavior as a value of false.
   */
  @Field val UNIQUE_ITEMS = booleanKeyword("uniqueItems")
      .expects(JsrType.BOOLEAN).validates(JsonSchemaType.ARRAY)
      .build()

  /**
   * The value of this keyword MUST be a valid JSON Schema.
   *
   *
   * An array instance is valid against "contains" if at least one of
   * its elements is valid against the given schema.
   */
  @Global
  val CONTAINS = singleSchemaKeyword("contains").expects(JsrType.OBJECT).validates(JsonSchemaType.ARRAY).since(Draft6).build()

  /**
   * The value of this keyword MUST be an array. This array SHOULD have at
   * least one element. Elements in the array SHOULD be unique.
   *
   *
   * An instance validates successfully against this keyword if its value is
   * equal to one of the elements in this keyword's array value.
   *
   *
   * Elements in the array might be of any value, including null.
   */
  @Field val ENUM = jsonArrayKeyword("enum").expects(JsrType.ARRAY).build()

  /**
   * The value of this keyword MUST be an array. There are no restrictions placed on the values within the array.
   *
   *
   * This keyword can be used to provide sample JSON values associated with a particular schema, for the purpose
   * of illustrating usage. It is RECOMMENDED that these values be valid against the associated schema.
   *
   *
   * Implementations MAY use the value of "default", if present, as an additional example. If "examples" is
   * absent, "default" MAY still be used in this manner.
   */
  @Global
  val EXAMPLES = jsonArrayKeyword("examples").expects(JsrType.ARRAY).since(Draft6).build()

  /**
   * The value of this keyword MAY be of any type, including null.
   *
   *
   * An instance validates successfully against this keyword if its value is
   * equal to the value of the keyword.
   */
  @Field val CONST = jsonValueKeyword("const").expects(JsrType.OBJECT).since(Draft6)
      .additionalDefinition().expects(JsrType.ARRAY).since(Draft6)
      .additionalDefinition().expects(JsrType.STRING).since(Draft6)
      .additionalDefinition().expects(JsrType.NUMBER).since(Draft6)
      .additionalDefinition().expects(JsrType.BOOLEAN).since(Draft6)
      .additionalDefinition().expects(JsrType.NULL).since(Draft6)
      .build()

  /**
   * This keyword's value MUST be a valid JSON Schema.
   *
   *
   * An instance is valid against this keyword if it fails to validate
   * successfully against the schema defined by this keyword.
   */
  @Field val NOT = singleSchemaKeyword("not").expects(JsrType.OBJECT).since(Draft4).build()

  /**
   * 6.6.1. if
   * This keyword's value MUST be a valid JSON Schema.
   *
   * Instances that successfully validate against this keyword's subschema MUST also be valid against
   * the subschema value of the "then" keyword, if present.
   *
   * Instances that fail to validate against this keyword's subschema MUST also be valid against the
   * subschema value of the "else" keyword.
   *
   * Validation of the instance against this keyword on its own always succeeds, regardless of the
   * validation outcome of against its subschema.
   */
  @Field val IF = singleSchemaKeyword("if")
      .expects(JsrType.OBJECT)
      .since(Draft7)
      .build()

  /**
   * 6.6.2. then
   * This keyword's value MUST be a valid JSON Schema.
   *
   * When present alongside of "if", the instance successfully validates against this keyword if it
   * validates against both the "if"'s subschema and this keyword's subschema.
   *
   * When "if" is absent, or the instance fails to validate against its subschema, validation
   * against this keyword always succeeds. Implementations SHOULD avoid attempting to validate
   * against the subschema in these cases.
   */
  @Field val THEN = singleSchemaKeyword("then")
      .since(Draft7)
      .build()

  /**
   * 6.6.3. else
   * This keyword's value MUST be a valid JSON Schema.
   *
   * When present alongside of "if", the instance successfully validates against this keyword if it
   * fails to validate against the "if"'s subschema, and successfully validates against this keyword's
   * subschema.
   *
   * When "if" is absent, or the instance successfully validates against its subschema, validation
   * against this keyword always succeeds. Implementations SHOULD avoid attempting to validate against
   * the subschema in these cases.
   */
  @Field val ELSE = singleSchemaKeyword("else")
      .since(Draft7)
      .build()

  /**
   * This keyword's value MUST be a non-empty array.
   * Each item of the array MUST be a valid JSON Schema.
   *
   *
   * An instance validates successfully against this keyword if it validates
   * successfully against all schemas defined by this keyword's value.
   */
  @Field val ALL_OF = schemaListKeyword("allOf")
      .since(Draft4).build()
  /**
   * This keyword's value MUST be a non-empty array.
   * Each item of the array MUST be a valid JSON Schema.
   *
   *
   * An instance validates successfully against this keyword if it validates
   * successfully against at least one schema defined by this keyword's value.
   */
  @Field val ANY_OF = schemaListKeyword("anyOf")
      .expects(JsrType.ARRAY)
      .since(Draft4).build()
  /**
   * This keyword's value MUST be a non-empty array.
   * Each item of the array MUST be a valid JSON Schema.
   *
   *
   * An instance validates successfully against this keyword if it validates
   * successfully against exactly one schema defined by this keyword's value.
   */
  @Field val ONE_OF = schemaListKeyword("oneOf").expects(JsrType.ARRAY).since(Draft4).build()

  /**
   * Can be used to inspect all keywords
   */
  val all:List<KeywordInfo<*>> = keywordCollector.freezeList()

  fun schemaMapKeyword(keyword: String): KeywordInfo.KeywordInfoBuilder<SchemaMapKeyword> {
    return KeywordInfoBuilder<SchemaMapKeyword>().key(keyword)
  }

  fun stringKeyword(keyword: String): KeywordInfo.KeywordInfoBuilder<StringKeyword> {
    return KeywordInfoBuilder<StringKeyword>().key(keyword).expects(JsrType.STRING)
  }

  fun jsonValueKeyword(keyword: String): KeywordInfo.KeywordInfoBuilder<JsonValueKeyword> {
    return KeywordInfoBuilder<JsonValueKeyword>().key(keyword)
  }

  fun numberKeyword(keyword: String): KeywordInfoBuilder<NumberKeyword> {
    return KeywordInfoBuilder<NumberKeyword>().key(keyword).expects(JsrType.NUMBER)
  }

  fun stringSetKeyword(keyword: String): KeywordInfoBuilder<StringSetKeyword> {
    return KeywordInfoBuilder<StringSetKeyword>().key(keyword).expects(JsrType.ARRAY)
  }

  fun singleSchemaKeyword(keyword: String): KeywordInfoBuilder<SingleSchemaKeyword> {
    return KeywordInfoBuilder<SingleSchemaKeyword>().key(keyword).expects(JsrType.OBJECT)
  }

  fun schemaListKeyword(keyword: String): KeywordInfoBuilder<SchemaListKeyword> {
    return KeywordInfoBuilder<SchemaListKeyword>().key(keyword).expects(JsrType.ARRAY)
  }

  fun uriKeyword(keyword: String): KeywordInfoBuilder<URIKeyword> {
    return KeywordInfoBuilder<URIKeyword>().key(keyword).expects(JsrType.STRING)
  }

  fun booleanKeyword(keyword: String): KeywordInfoBuilder<BooleanKeyword> {
    return KeywordInfoBuilder<BooleanKeyword>().key(keyword).expects(JsrType.BOOLEAN)
  }

  fun jsonArrayKeyword(keyword: String): KeywordInfoBuilder<JsonArrayKeyword> {
    return KeywordInfoBuilder<JsonArrayKeyword>().key(keyword).expects(JsrType.ARRAY)
  }

  inline fun <reified X : Keyword<*>> keyword(): KeywordInfoBuilder<X> {
    return KeywordInfoBuilder()
  }
}

