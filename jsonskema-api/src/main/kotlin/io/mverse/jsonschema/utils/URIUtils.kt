package io.mverse.jsonschema.utils

import lang.URI
import lang.isFragmentOnly
import lang.toHex

object URIUtils {

  val SCHEME_AUTOASSIGN = "mverse"

  fun withNewFragment(existing: URI, newFragment: URI): URI {
    check(newFragment.isFragmentOnly) { "Must only be a fragment" }
    return withFragment(existing, newFragment.fragment)
  }

  fun isJsonPointer(uri: URI): Boolean {
    if (uri.isFragmentOnly) {
      val fragment = uri.fragment
      return fragment?.isEmpty() == true || fragment?.startsWith("/") == true
    }
    return false
  }

  fun trimEmptyFragment(uri: URI): URI {
    return if (uri.fragment.isNullOrEmpty())
      withoutFragment(uri)
    else uri
  }

  private fun withFragment(uri: URI, fragment: String?): URI {
    return if (uri.fragment == null && fragment.isNullOrEmpty()) {
      uri
    } else URI(uri.scheme, uri.schemSpecificPart, fragment)
  }

  fun withoutFragment(uri: URI): URI {
    return withFragment(uri, null)
  }

  fun resolve(base: URI, against: URI): URI {
    return if (base.isOpaque && against.isFragmentOnly) {
      base.withNewFragment(against)
    } else {
      base.resolve(against)
    }
  }

  fun generateUniqueURI(forInstance: Any): URI {
    val hashed = StringBuilder()
    hashed.append(forInstance.hashCode().toHex())
    hashed.append("-")
    hashed.append(forInstance.toString().length)

    return URI(SCHEME_AUTOASSIGN, "//$hashed/schema", null)
  }

  fun isGeneratedURI(uri: URI): Boolean {
    return SCHEME_AUTOASSIGN.equals(uri.scheme)
  }
}
