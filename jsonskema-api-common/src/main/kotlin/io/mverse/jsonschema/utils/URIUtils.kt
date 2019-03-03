package io.mverse.jsonschema.utils

import io.ktor.client.HttpClient
import kotlinx.io.core.Input
import lang.net.URI
import lang.net.fragment
import lang.net.isFragmentOnly
import lang.net.scheme
import lang.net.schemeSpecificPart
import lang.toHex

const val SCHEME_AUTOASSIGN = "mvuuid"

fun URI.trimEmptyFragment(): URI {
  return if (this.fragment.isNullOrEmpty())
    this.withoutFragment()
  else {
    this
  }
}

expect suspend fun URI.httpGet(): ByteArray

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


