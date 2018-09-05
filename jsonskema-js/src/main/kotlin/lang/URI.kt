package lang

/**
 * Copied many parts of java.net.URI to ensure the same behavior
 */
actual class URI(back: Map<String, Any?> = emptyMap(), vararg overrides: Pair<String, Any?>) {
  val backing: Map<String, Any?> = back.toMutableMap().apply {
    overrides.forEach {
      this[it.first] = it.second
    }
  }

  constructor(uri: String) : this(back = parseUri(uri).backing)

  actual constructor(scheme: String?, schemeSpecificPart: String?, fragment: String?) :
      this(uri = (scheme ?: "") + (schemeSpecificPart ?: "") + "#" + (fragment ?: ""))

  val source: String? by backing
  val protocol: String? by backing
  val authority: String? by backing
  val userInfo: String? by backing
  val user: String? by backing
  val password: String? by backing
  val host: String? by backing
  val port: Int?
    get() = backing["port"]?.toString()?.toInt()
  val relative: String? by backing
  val path: String? by backing
  val directory: String? by backing
  val file: String? by backing
  val query: String? by backing
  val anchor: String? by backing

  actual constructor(uri: String?) : this(null, null, null)

  actual fun isAbsolute(): Boolean = scheme != null
  actual fun isOpaque(): Boolean = path == null

  // Normalize the given path string.  A normal path string has no empty
  // segments (i.e., occurrences of "//"), no segments equal to ".", and no
  // segments equal to ".." that are preceded by a segment not equal to "..".
  // In contrast to Unix-style pathname normalization, for URI paths we
  // always retain trailing slashes.
  //
  fun normalize(psn: String?): String {

    val ps = psn ?: return ""

    // Does this path need normalization?
    val ns = needsNormalization(ps)        // Number of segments
    if (ns < 0)
    // Nope -- just return it
      return ps

    val path = ps.toList().toCharArray()         // Path in char-array form

    // Split path into segments
    val segs = IntArray(ns)               // Segment-index array
    split(path, segs)

    // Remove dots
    removeDots(path, segs)

    // Prevent scheme-name confusion
    maybeAddLeadingDot(path, segs)

    // Join the remaining segments and return the result
    val s = String(path, 0, join(path, segs))
    return if (s == ps) {
      // string was already normalized
      ps
    } else s
  }

  // If both URIs are hierarchical, their scheme and authority components are
  // identical, and the base path is a prefix of the child's path, then
  // return a relative URI that, when resolved against the base, yields the
  // child; otherwise, return the child.
  //
  actual fun relativize(child: URI): URI {
    val base = this
    // check if child if opaque first so that NPE is thrown
    // if child is null.
    if (child.isOpaque() || base.isOpaque())
      return child
    if (!base.scheme.equals(child.scheme, ignoreCase = true) || !base.authority.equals(child.authority)) {
      return child
    }

    var bp = normalize(base.path)
    val cp = normalize(child.path)
    if (bp != cp) {
      if (!bp.endsWith("/"))
        bp += "/"
      if (!cp.startsWith(bp))
        return child
    }

    return URI(emptyMap(),
        "path" to cp.substring(bp.length),
        "query" to child.query,
        "anchor" to child.anchor
    )
  }

  private fun normalize(u: URI): URI {
    if (u.isOpaque() || u.path == null || u.path?.isEmpty() == true)
      return u

    val np = normalize(u.path)
    if (np === u.path) {
      return u
    }

    return URI(emptyMap(),
        "scheme" to u.scheme,
        "fragment" to u.fragment,
        "authority" to u.authority,
        "userInfo" to u.userInfo,
        "host" to u.host,
        "port" to u.port,
        "path" to np,
        "query" to u.query)
  }

  private fun needsNormalization(path: String): Int {
    var normal = true
    var ns = 0                     // Number of segments
    val end = path.length - 1    // Index of last char in path
    var p = 0                      // Index of next char in path

    // Skip initial slashes
    while (p <= end) {
      if (path[p] != '/') break
      p++
    }
    if (p > 1) normal = false

    // Scan segments
    while (p <= end) {

      // Looking at "." or ".." ?
      if (path[p] == '.' && (p == end || path[p + 1] == '/' || path[p + 1] == '.' && (p + 1 == end || path[p + 2] == '/'))) {
        normal = false
      }
      ns++

      // Find beginning of next segment
      while (p <= end) {
        if (path[p++] != '/')
          continue

        // Skip redundant slashes
        while (p <= end) {
          if (path[p] != '/') break
          normal = false
          p++
        }

        break
      }
    }

    return if (normal) -1 else ns
  }

  // Split the given path into segments, replacing slashes with nulls and
  // filling in the given segment-index array.
  //
  // Preconditions:
  //   segs.length == Number of segments in path
  //
  // Postconditions:
  //   All slashes in path replaced by '\0'
  //   segs[i] == Index of first char in segment i (0 <= i < segs.length)
  //
  private fun split(path: CharArray, segs: IntArray) {
    val end = path.size - 1      // Index of last char in path
    var p = 0                      // Index of next char in path
    var i = 0                      // Index of current segment

    // Skip initial slashes
    while (p <= end) {
      if (path[p] != '/') break
      path[p] = '\u0000'
      p++
    }

    while (p <= end) {

      // Note start of segment
      segs[i++] = p++

      // Find beginning of next segment
      while (p <= end) {
        if (path[p++] != '/')
          continue
        path[p - 1] = '\u0000'

        // Skip redundant slashes
        while (p <= end) {
          if (path[p] != '/') break
          path[p++] = '\u0000'
        }
        break
      }
    }

    if (i != segs.size)
      throw Exception()  // ASSERT
  }

  // Join the segments in the given path according to the given segment-index
  // array, ignoring those segments whose index entries have been set to -1,
  // and inserting slashes as needed.  Return the length of the resulting
  // path.
  //
  // Preconditions:
  //   segs[i] == -1 implies segment i is to be ignored
  //   path computed by split, as above, with '\0' having replaced '/'
  //
  // Postconditions:
  //   path[0] .. path[return value] == Resulting path
  //
  private fun join(path: CharArray, segs: IntArray): Int {
    val ns = segs.size           // Number of segments
    val end = path.size - 1      // Index of last char in path
    var p = 0                      // Index of next path char to write

    if (path[p] == '\u0000') {
      // Restore initial slash for absolute paths
      path[p++] = '/'
    }

    for (i in 0 until ns) {
      var q = segs[i]            // Current segment
      if (q == -1)
      // Ignore this segment
        continue

      if (p == q) {
        // We're already at this segment, so just skip to its end
        while (p <= end && path[p] != '\u0000')
          p++
        if (p <= end) {
          // Preserve trailing slash
          path[p++] = '/'
        }
      } else if (p < q) {
        // Copy q down to p
        while (q <= end && path[q] != '\u0000')
          path[p++] = path[q++]
        if (q <= end) {
          // Preserve trailing slash
          path[p++] = '/'
        }
      } else
        throw Exception() // ASSERT false
    }

    return p
  }

  // Remove "." segments from the given path, and remove segment pairs
  // consisting of a non-".." segment followed by a ".." segment.
  //
  private fun removeDots(path: CharArray, segs: IntArray) {
    val ns = segs.size
    val end = path.size - 1

    var i = 0
    while (i < ns) {
      var dots = 0               // Number of dots found (0, 1, or 2)

      // Find next occurrence of "." or ".."
      do {
        val p = segs[i]
        if (path[p] == '.') {
          if (p == end) {
            dots = 1
            break
          } else if (path[p + 1] == '\u0000') {
            dots = 1
            break
          } else if (path[p + 1] == '.' && (p + 1 == end || path[p + 2] == '\u0000')) {
            dots = 2
            break
          }
        }
        i++
      } while (i < ns)
      if (i > ns || dots == 0)
        break

      if (dots == 1) {
        // Remove this occurrence of "."
        segs[i] = -1
      } else {
        // If there is a preceding non-".." segment, remove both that
        // segment and this occurrence of ".."; otherwise, leave this
        // ".." segment as-is.
        var j: Int
        j = i - 1
        while (j >= 0) {
          if (segs[j] != -1) break
          j--
        }
        if (j >= 0) {
          val q = segs[j]
          if (!(path[q] == '.'
                  && path[q + 1] == '.'
                  && path[q + 2] == '\u0000')) {
            segs[i] = -1
            segs[j] = -1
          }
        }
      }
      i++
    }
  }

  // DEVIATION: If the normalized path is relative, and if the first
  // segment could be parsed as a scheme name, then prepend a "." segment
  //
  private fun maybeAddLeadingDot(path: CharArray, segs: IntArray) {

    if (path[0] == '\u0000')
    // The path is absolute
      return

    val ns = segs.size
    var f = 0                      // Index of first segment
    while (f < ns) {
      if (segs[f] >= 0)
        break
      f++
    }
    if (f >= ns || f == 0)
    // The path is empty, or else the original first segment survived,
    // in which case we already know that no leading "." is needed
      return

    var p = segs[f]
    while (p < path.size && path[p] != ':' && path[p] != '\u0000') p++
    if (p >= path.size || path[p] == '\u0000')
    // No colon in first segment, so no "." needed
      return

    // At this point we know that the first segment is unused,
    // hence we can insert a "." segment at that position
    path[0] = '.'
    path[1] = '\u0000'
    segs[0] = 0
  }

  actual fun getFragment(): String? = anchor

  actual fun getScheme(): String? = source
  actual fun getSchemeSpecificPart(): String? = relative
  actual fun getQuery(): String? = query
  actual fun getPath(): String? = path

  fun resolve(against: URI): URI {
    val base = this
    val child = against
    // check if child if opaque first so that NPE is thrown
    // if child is null.
    if (child.isOpaque() || base.isOpaque())
      return child

    // 5.2 (2): Reference to current document (lone fragment)
    if (child.scheme == null && child.authority == null
        && child.path == "" && child.fragment != null
        && child.query == null) {
      if (base.fragment != null && child.fragment == base.fragment) {
        return base
      }
      val ru = URI(emptyMap(),
          "scheme" to base.scheme,
          "authority" to base.authority,
          "userInfo" to base.userInfo,
          "host" to base.host,
          "port" to base.port,
          "path" to base.path,
          "fragment" to child.fragment,
          "query" to base.query
      )

      return ru
    }

    // 5.2 (3): Child is absolute
    if (child.scheme != null)
      return child

    val ru = mutableMapOf<String, Any?>(
        "scheme" to base.scheme,
        "query" to child.query,
        "fragment" to child.fragment)

    // 5.2 (4): Authority
    if (child.authority == null) {
      ru["authority"] = base.authority
      ru["host"] = base.host
      ru["userInfo"] = base.userInfo
      ru["port"] = base.port

      val cp = child.path ?: ""
      if (cp.isNotEmpty() && cp[0] == '/') {
        // 5.2 (5): Child path is absolute
        ru["path"] = child.path
      } else {
        // 5.2 (6): Resolve relative path
        ru["path"] = resolvePath(base.path ?: "", cp, base.isAbsolute())
      }
    } else {
      ru["authority"] = child.authority
      ru["host"] = child.host
      ru["userInfo"] = child.userInfo
      ru["host"] = child.host
      ru["port"] = child.port
      ru["path"] = child.path
    }

    // 5.2 (7): Recombine (nothing to do here)
    return URI(ru)
  }

  private fun resolvePath(base: String, child: String,
                          absolute: Boolean): String {
    val i = base.lastIndexOf('/')
    val cn = child.length
    var path = ""

    if (cn == 0) {
      // 5.2 (6a)
      if (i >= 0)
        path = base.substring(0, i + 1)
    } else {
      val sb = StringBuilder(base.length + cn)
      // 5.2 (6a)
      if (i >= 0)
        sb.append(base.substring(0, i + 1))
      // 5.2 (6b)
      sb.append(child)
      path = sb.toString()
    }

    // 5.2 (6c-f)

    // 5.2 (6g): If the result is absolute but the path begins with "../",
    // then we simply leave the path as-is

    return normalize(path)
  }
}

actual fun URI.readFully(charset: String): String {
  TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

actual fun URI.withNewFragment(newFragment: URI): URI {
  check(newFragment.isFragmentOnly) { "Must only be a fragment" }
  return URI(scheme, schemeSpecificPart, newFragment.fragment)
}

actual fun URI.resolveUri(against: URI): URI = this.resolve(against)

actual object URLDecoder {
  actual fun decode(input: String, charset: String): String {
    return decodeURIComponent(input)
  }
}

val strict = Regex("^(?:([^:/?#]+):)?(?://((?:(([^:@]*)(?::([^:@]*))?)?@)?([^:/?#]*)(?::(\\d*))?))?((((?:[^?#/]*/)*)([^?#]*))(?:\\?([^#]*))?(?:#(.*))?)")
val loose = Regex("^(?:(?![^:@]+:[^:@/]*@)([^:/?#.]+):)?(?://)?((?:(([^:@]*)(?::([^:@]*))?)?@)?([^:/?#]*)(?::(\\d*))?)(((/(?:[^?#](?![^?#/]*\\.[^?#/.]+(?:[?#]|$)))*/?)?([^?#/]*))(?:\\?([^#]*))?(?:#(.*))?)")

data class QParser(val name: String = "queryKey",
                   val parser: Regex = Regex("(?:^|&)([^&=]*)=?([^&]*)"))

data class Opts(val strictMode: Boolean = false,
                val key: List<String> = listOf("source", "protocol", "authority", "userInfo", "user", "password", "host", "port", "relative", "path", "directory", "file", "query", "anchor"),
                val q: QParser = QParser()) {
  fun parse(str: String): MatchResult? = when (strictMode) {
    true -> strict.matchEntire(str)
    false -> loose.matchEntire(str)
  }
}

object parseUri {
  var options = Opts()

  operator fun invoke(str: String): URI {
    val o = parseUri.options
    val m = o.parse(str) ?: return URI(emptyMap())

    val uri = mutableMapOf<String, Any>()
    var g = 0
    for (key in o.key) {
      uri[key] = m.groupValues.getOrNull(g++) ?: ""
    }

    val names = mutableMapOf<String, String>()
    uri[o.key[12]].toString().replace(o.q.parser) { match ->
      names.put(match.range.start.toString(), match.value) ?: ""
    }

    return URI(uri)
  }
}
