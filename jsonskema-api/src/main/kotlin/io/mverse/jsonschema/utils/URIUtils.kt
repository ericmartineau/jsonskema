package io.mverse.jsonschema.utils

import lang.URI
import lang.isFragmentOnly
import lang.toHex

fun URI.trimEmptyFragment(): URI {
  return if (this.fragment.isNullOrEmpty())
    this.withoutFragment()
  else this
}

fun URI.isJsonPointer(): Boolean {
  val uri = this

  if (uri.isFragmentOnly) {
    val fragment = uri.fragment
    return fragment?.isEmpty() == true || fragment?.startsWith("/") == true
  }
  return false
}

private fun URI.withFragment(fragment: String?): URI {
  val uri = this
  return if (uri.fragment == null && fragment.isNullOrEmpty()) {
    uri
  } else URI(uri.scheme, uri.schemSpecificPart, fragment)
}

fun URI.withoutFragment(): URI {
  return withFragment(null)
}


fun generateUniqueURI(forInstance: Any): URI {
  val hashed = StringBuilder()
  hashed.append(forInstance.hashCode().toHex())
  hashed.append("-")
  hashed.append(forInstance.toString().length)

  return URI(URIUtils.SCHEME_AUTOASSIGN, "//$hashed/schema", null)
}

fun URI.isGeneratedURI(): Boolean {
  val uri = this
  return URIUtils.SCHEME_AUTOASSIGN.equals(uri.scheme)
}

fun URI.withNewFragment(newFragment: URI): URI {
  check(newFragment.isFragmentOnly) { "Must only be a fragment" }
  return this.withFragment(newFragment.fragment)
}

object URIUtils {

  val SCHEME_AUTOASSIGN = "mverse"
}
