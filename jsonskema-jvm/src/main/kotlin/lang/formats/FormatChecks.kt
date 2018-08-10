package lang.formats

import com.damnhandy.uri.template.UriTemplate
import com.google.common.net.InetAddresses
import com.google.common.net.InternetDomainName
import com.google.i18n.phonenumbers.PhoneNumberUtil
import lang.illegalState
import org.apache.commons.validator.routines.EmailValidator
import java.net.InetAddress
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

actual object FormatChecks {

  const val IPV4_LENGTH = 4
  private val IPV6_LENGTH = 16

  private val isoDateChecker: DateTimeFormatter = DateTimeFormatterBuilder()
      .appendPattern(ISO_DATE_FORMAT)
      .toFormatter()

  private val isoDateTimeChecker: DateTimeFormatter
  private val isoTimeChecker: DateTimeFormatter

  init {
    val secondsFractionFormatter = DateTimeFormatterBuilder()
        .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
        .toFormatter()

    isoDateTimeChecker = DateTimeFormatterBuilder()
        .appendPattern(PARTIAL_DATETIME_PATTERN)
        .appendOptional(secondsFractionFormatter)
        .appendPattern(ZONE_OFFSET_PATTERN)
        .toFormatter()


    isoTimeChecker = DateTimeFormatterBuilder()
        .appendPattern(PARTIAL_TIME_PATTERN)
        .appendOptional(secondsFractionFormatter)
        .appendPattern(ZONE_OFFSET_PATTERN)
        .toFormatter()
  }

  actual fun isValidIsoDateTime(str: String): Boolean {
    isoDateTimeChecker.parse(str)
    return true
  }

  actual fun isValidIsoDate(str: String): Boolean {
    isoDateChecker.parse(str)
    return true
  }

  actual fun isValidIsoTime(str: String): Boolean {
    isoTimeChecker.parse(str)
    return true
  }

  actual fun isValidEmail(str: String): Boolean {
    return EmailValidator.getInstance(false, true).isValid(str)
  }

  actual fun isValidPhone(str: String): Boolean {
    val phone = PhoneNumberUtil.getInstance().parse(str, "US")
    return PhoneNumberUtil.getInstance().isValidNumber(phone)
  }

  actual fun isValidUriTemplate(str: String): Boolean {
    UriTemplate.fromTemplate(str)
    return true
  }

  actual fun isValidHostname(str: String): Boolean {
    InternetDomainName.from(str)
    return true
  }

  actual fun isValidIPV4(str: String): Boolean {
    return asInetAddress(str)?.address?.size == IPV4_LENGTH
  }

  actual fun isValidIPV6(str: String): Boolean {
    return asInetAddress(str)?.address?.size == IPV6_LENGTH
  }

  /**
   * Creates an [InetAddress] instance if possible and returns it, or on failure it returns
   * `null`.
   *
   * @param subject the string to be validated.
   * @return the optional validation failure message
   */
  private  fun asInetAddress(subject: String):InetAddress? {
    try {
      return if (InetAddresses.isInetAddress(subject)) {
        InetAddresses.forString(subject)
      } else {
        null
      }
    } catch (e: NullPointerException) {
      return null
    }
  }

}
