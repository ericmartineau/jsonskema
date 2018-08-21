package io.mverse.jsonschema

import io.mverse.jsonschema.utils.CharUtils
import io.mverse.jsonschema.utils.CharUtils.escapeForJsonPointerSegment
import io.mverse.jsonschema.utils.CharUtils.forwardSlashSeparator
import io.mverse.jsonschema.utils.CharUtils.jsonPointerSegmentEscaper
import io.mverse.jsonschema.utils.CharUtils.jsonPointerSegmentUnescaper
import io.mverse.jsonschema.utils.CharUtils.urlSegmentUnescaper
import kotlinx.serialization.KInput
import kotlinx.serialization.KOutput
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import lang.Escapers.Companion.urlPathSegmentEscaper
import lang.Joiner
import lang.Global
import lang.URI
import lang.Unescaper
import lang.hashKode

@Serializable
class JsonPath : Iterable<String> {

  private val segments: Array<String>

  private var uriFragment: URI? = null

  private var jsonPointerString: String? = null

  val lastPath: String?
    get() {
      val length = this.segments.size
      return if (length > 0) {
        this.segments[length - 1]
      } else {
        null
      }
    }

  val firstPath: String?
    get() {
      val length = this.segments.size
      return if (length > 0) {
        this.segments[0]
      } else {
        null
      }
    }

  override fun equals(other: Any?): Boolean {
    return other is JsonPath && other.segments.contentEquals(segments)
  }

  override fun hashCode(): Int {
    @Suppress("USELESS_ELVIS")
    return hashKode(*(segments ?: arrayOf()))
  }

  internal constructor(input: String, vararg unescapers: Unescaper) : this(input, false, *unescapers) {}

  /**
   * This method ingests a segments-separated string intended as a json-pointer.  The string may be based on a URL fragment,
   * and as such may contain escape sequences, (such as %25 to escape /).
   *
   *
   * This method assumes that any json-pointer segments are escaped.  Any other escaping, eg URL encoding must be specified
   * by providing appropriate unescapers.  Any provided unescapers will be processed before the json-pointer unescapers
   *
   * @param input      Valid escaped json-pointer string
   * @param relative   Whether the pointer should be interpreted as relative
   * @param unescapers Additional unescapers for each segment, ie url segment decoder.
   */
  internal constructor(input: String, relative: Boolean, vararg unescapers: Unescaper) {
    check(relative || input.isEmpty() || input.startsWith("/")) {
      "invalid json-pointer syntax.  Must either be blank or start with a /"
    }

    val fragmentURI = StringBuilder("#")
    val jsonPointer = StringBuilder("")

    val parts = mutableListOf<String>()
    for (rawPart in forwardSlashSeparator().split(input)) {
      if (rawPart.isEmpty() && !parts.isEmpty()) {
        throw IllegalArgumentException("invalid blank segment in json-pointer")
      } else if (rawPart.isEmpty()) {
        continue
      }

      var toUnescape = rawPart
      for (unescaper in unescapers) {
        toUnescape = unescaper.unescape(toUnescape)!!
      }

      val pathPart = jsonPointerSegmentUnescaper().unescape(toUnescape)!!

      //Re-escape, because we can't rely on what was escaped coming in.
      val pointerEscaped = jsonPointerSegmentEscaper().escape(pathPart)

      jsonPointer.append("/").append(pointerEscaped)
      fragmentURI.append("/").append(urlPathSegmentEscaper().escape(pointerEscaped))

      parts.add(pathPart)
    }
    this.segments = parts.toTypedArray()
    this.uriFragment = URI(fragmentURI.toString())
    this.jsonPointerString = jsonPointer.toString()
  }

  internal constructor(parts: Array<String>, toBeAppended: Int) {
    this.segments = parts + toBeAppended.toString()
  }

  internal constructor(parts: Array<String>, vararg toBeAppended: String) {
    var segments = parts.copyOf()
    for (i in toBeAppended.indices) {
      segments += toBeAppended[i]
    }
    this.segments = segments
  }

  internal constructor(parts: Array<String>, toBeAppended: String) {
    this.segments = parts + toBeAppended
  }

  fun child(unescapedPath: String): JsonPath {
    return JsonPath(this.segments, unescapedPath)
  }

  fun child(vararg unescapedPath: String): JsonPath {
    return JsonPath(this.segments, *unescapedPath)
  }

  fun child(index: Int): JsonPath {
    return JsonPath(this.segments, index)
  }

  fun toJsonPointer(): String {
    if (jsonPointerString == null) {
      val jsonPointer = StringBuilder("")
      for (segment in segments) {
        jsonPointer.append("/").append(escapeForJsonPointerSegment(segment))
      }
      this.jsonPointerString = jsonPointer.toString()
    }
    return jsonPointerString!!
  }

  override fun toString(): String {
    return toURIFragment().toString()
  }

  fun toStringPath(): List<String> {
    return this.segments.toList()
  }

  fun toURIFragment(): URI {
    if (this.uriFragment == null) {
      val uriFragment = StringBuilder("#")
      for (pathPart in segments) {
        val escaped = CharUtils.escapeForURIPointerSegment(pathPart)
        uriFragment.append("/").append(escaped)
      }
      this.uriFragment = URI(uriFragment.toString())
    }
    return this.uriFragment!!
  }

  fun toString(joiner: Joiner): String {
    return joiner.join(segments)
  }

  override fun iterator(): Iterator<String> = segments.iterator()

  fun forEach(block: (String)->Unit) {
    for (segment in segments) {
      block(segment)
    }
  }

  @Serializer(forClass = JsonPath::class)
  companion object {

    override fun save(output: KOutput, obj: JsonPath) {
      output.writeStringValue(obj.toJsonPointer())
    }

    override fun load(input: KInput): JsonPath {
      return JsonPath.parseJsonPointer(input.readStringValue())
    }

    @Global
    fun parseFromURIFragment(uriFragment: URI): JsonPath {
      return parseFromURIFragment(uriFragment.toString())
    }

    @Global
    fun parseFromURIFragment(uriFragment: String): JsonPath {

      check("".equals(uriFragment) || "#".equals(uriFragment) || uriFragment.startsWith("#/")) {
        "URI Fragment invalid: $uriFragment"
      }
      return if (uriFragment.isNullOrEmpty() || uriFragment == "#") JsonPath.rootPath()
      else JsonPath(uriFragment.substring(1), urlSegmentUnescaper())
    }

    @Global
    fun parseJsonPointer(jsonPointer: String): JsonPath {
      return JsonPath(jsonPointer)
    }

    @Global
    fun parseRelativeJsonPointer(jsonPointer: String): JsonPath {
      return JsonPath(jsonPointer, true)
    }

    @Global
    fun rootPath(): JsonPath {
      return JsonPath("")
    }
  }
}
