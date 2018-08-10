package lang.formats

val ISO_DATE_FORMAT = "yyyy-MM-dd"
val DATETIME_FORMATS_ACCEPTED = listOf(
    "yyyy-MM-dd'T'HH:mm:ssZ", "yyyy-MM-dd'T'HH:mm:ss.[0-9]{1,9}Z"
)
val PARTIAL_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss"
val ZONE_OFFSET_PATTERN = "XXX"

val TIME_FORMATS_ACCEPTED = listOf("HH:mm:ssZ", "HH:mm:ss.[0-9]{1,9}Z")
val PARTIAL_TIME_PATTERN = "HH:mm:ss"

expect object FormatChecks {

  fun isValidIsoDateTime(str: String): Boolean
  fun isValidIsoDate(str: String): Boolean
  fun isValidIsoTime(str: String): Boolean
  fun isValidEmail(str: String): Boolean
  fun isValidPhone(str: String): Boolean
  fun isValidUriTemplate(str: String): Boolean
  fun isValidHostname(str: String): Boolean
  fun isValidIPV4(str: String):Boolean
  fun isValidIPV6(str: String):Boolean
}
