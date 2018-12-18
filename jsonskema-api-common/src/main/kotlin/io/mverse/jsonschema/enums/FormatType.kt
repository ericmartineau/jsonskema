package io.mverse.jsonschema.enums

import io.mverse.jsonschema.enums.JsonSchemaVersion.Companion.latest
import io.mverse.jsonschema.enums.JsonSchemaVersion.Draft3
import io.mverse.jsonschema.enums.JsonSchemaVersion.Draft4
import io.mverse.jsonschema.enums.JsonSchemaVersion.Draft5
import io.mverse.jsonschema.enums.JsonSchemaVersion.Draft6
import io.mverse.jsonschema.enums.JsonSchemaVersion.Draft7
import lang.range

/**
 * Represents each of the built-in format types in the json-schema specification.
 */
enum class FormatType(key: String? = null,
                      val applicableVersions: Set<JsonSchemaVersion> = JsonSchemaVersion.values().toSet()) {

  /**
   * from draft-06
   *
   *
   * email  A string instance is valid against this attribute if it is a valid
   * Internet email address as defined by RFC 5322, section 3.4.1
   * [RFC5322].
   */
  EMAIL(since = Draft3),

  /**
   * from draft-07
   *
   *
   * idn-email As defined by RFC 6531 [RFC6531]
   */
  IDN_EMAIL(since = Draft7),

  /**
   * from draft-06
   * A string instance is valid against this attribute if it is a valid
   * date representation as defined by RFC 3339, section 5.6 [RFC3339].
   */
  DATE_TIME(since = Draft3),

  /**
   * from draft-06
   *
   *
   * A string instance is valid against this attribute if it is a valid
   * representation for an Internet host name, as defined by RFC 1034,
   * section 3.1 [RFC1034].
   */
  HOSTNAME(since = Draft4),

  /**
   * As defined by either RFC 1034 as for hostname, or an internationalized
   * hostname as defined by RFC 5890, section 2.3.2.3 [RFC5890].
   */
  IDN_HOSTNAME(since = Draft7),

  /**
   * from draft-06
   *
   *
   * A string instance is valid against this attribute if it is a valid
   * representation of an IPv4 address according to the "dotted-quad" ABNF
   * syntax as defined in RFC 2673, section 3.2 [RFC2673].
   */
  IPV4(since = Draft4),

  /**
   * from draft-06
   *
   *
   * A string instance is valid against this attribute if it is a valid
   * representation of an IPv6 address as defined in RFC 2373, section 2.2
   * [RFC2373].
   */
  IPV6(since = Draft3),

  /**
   * A string instance is valid against this attribute if it is a valid IRI, according to [RFC3987].
   */
  IRI(since = Draft7),

  /**
   * A string instance is valid against this attribute if it is a valid IRI Reference (either an
   * IRI or a relative-reference), according to [RFC3987].
   */
  IRI_REFERENCE(since = Draft7),

  /**
   * from draft-06
   *
   *
   * A string instance is valid against this attribute if it is a valid
   * URI, according to [RFC3986].
   */
  URI(since = Draft3),

  /**
   * from draft-06
   *
   *
   * A string instance is valid against this attribute if it is a valid
   * URI Reference (either a URI or a relative-reference), according to
   * [RFC3986].
   */
  URI_REFERENCE(since = Draft6),

  /**
   * from draft-06
   *
   *
   * A string instance is valid against this attribute if it is a valid
   * URI Template (of any level), according to [RFC6570].
   */
  URI_TEMPLATE(since = Draft6),

  /**
   * from draft-06
   *
   *
   * A string instance is valid against this attribute if it is a valid
   * JSON Pointer, according to [RFC6901]
   */
  JSON_POINTER(since = Draft6),

  /**
   * A string instance is valid against this attribute if it is a valid Relative JSON Pointer
   * [relative-json-pointer].
   *
   * https://tools.ietf.org/html/draft-handrews-relative-json-pointer-00
   */
  RELATIVE_JSON_POINTER(since = Draft7),

  /**
   * from draft-03
   *
   *
   * date  This SHOULD be a date in the format of YYYY-MM-DD.  It is
   * recommended that you use the "date-time" format instead of "date"
   * unless you need to transfer only the date part.
   */
  DATE(applicableVersions = setOf(Draft3, Draft7)),

  /**
   * from draft-03
   *
   *
   * time  This SHOULD be a time in the format of hh:mm:ss.  It is
   * recommended that you use the "date-time" format instead of "time"
   * unless you need to transfer only the time part.
   */
  TIME(applicableVersions = setOf(Draft3, Draft7)),

  /**
   * from draft-03
   *
   *
   * This SHOULD be the difference, measured in
   * milliseconds, between the specified time and midnight, 00:00 of
   * January 1, 1970 UTC.  The value SHOULD be a number (Int or
   * float).
   */
  UTC_MILLISEC(since = Draft3, until = Draft3),

  /**
   * from draft-03
   *
   *
   * regex  A regular expression, following the regular expression
   * specification from ECMA 262/Perl 5.
   */
  REGEX(applicableVersions = setOf(Draft3, Draft7)),

  /**
   * from draft-03
   *
   *
   * phone  This SHOULD be a phone number (format MAY follow E.123).
   */
  PHONE(since = Draft3, until = Draft3),

  /**
   * from draft-03
   *
   *
   * color  This is a CSS color (like "#FF0000" or "red"), based on CSS
   * 2.1 [W3C.CR-CSS21-20070719].
   */
  COLOR(since = Draft3, until = Draft3),

  /**
   * from draft-03
   *
   *
   * style  This is a CSS style definition (like "color: red; background-
   * color:#FFF"), based on CSS 2.1 [W3C.CR-CSS21-20070719].
   */
  STYLE(since = Draft3, until = Draft3),

  /**
   * Renamed to uri-reference in draft-06
   *
   * See [.URI_REFERENCE]
   */
  URIREF(since = Draft5, until = Draft5),

  /**
   * Originally named host-name, this was renamed to hostname in draft-04
   * See [.HOSTNAME]
   */
  HOST_NAME(since = Draft3, until = Draft3),

  /**
   * Renamed to ipv4 in draft-04
   *
   *
   * See [.IPV4]
   */
  IP_ADDRESS(since = Draft3, until = Draft3);

  val value: String = key ?: name.toLowerHyphen()

  constructor(key: String? = null,
              since: JsonSchemaVersion = Draft3,
              until: JsonSchemaVersion = latest) :
      this(key, JsonSchemaVersion.values().range(since, until))

  override fun toString(): String = value

  companion object {
    fun fromFormat(format: String?): FormatType? {
      val potentialMatch = format?.toUpperUnderscore() ?: return null
      return try {
        FormatType.valueOf(potentialMatch)
      } catch (e: IllegalArgumentException) {
        return null
      }
    }
  }

}

internal fun String.toLowerHyphen(): String {
  return this.toLowerCase().replace('_', '-')
}

internal fun String.toUpperUnderscore(): String {
  return this.toUpperCase().replace('-', '_')
}
