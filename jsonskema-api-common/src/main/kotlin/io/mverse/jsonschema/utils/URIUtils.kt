package io.mverse.jsonschema.utils

import lang.net.URI
import lang.net.fragment
import lang.net.isFragmentOnly
import lang.net.scheme
import lang.net.schemeSpecificPart
import lang.toHex

const val SCHEME_AUTOASSIGN = "mverse"

fun URI.trimEmptyFragment(): URI {
  return if (this.fragment.isNullOrEmpty())
    this.withoutFragment()
  else {
    this
  }
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
  return if (uri.getFragment() == null && fragment.isNullOrEmpty()) {
    uri
  } else URI(uri.scheme, uri.schemeSpecificPart, fragment)
}

fun URI.withoutFragment(): URI {
  return withFragment(null)
}

fun generateUniqueURI(forInstance: Any): URI {
  val hashed = StringBuilder()
  hashed.append(forInstance.hashCode().toHex())
  hashed.append("-")
  hashed.append(forInstance.toString().length)

  return URI(SCHEME_AUTOASSIGN, "//$hashed/schema", null)
}

fun URI.isGeneratedURI(): Boolean {
  val uri = this
  return SCHEME_AUTOASSIGN.equals(uri.scheme)
}

fun URI.withNewFragment(newFragment: URI): URI {
  check(newFragment.isFragmentOnly) { "Must only be a fragment" }
  return this.withFragment(newFragment.fragment)
}

