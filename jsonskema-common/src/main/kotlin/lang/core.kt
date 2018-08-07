package lang

fun <A, B> A?.convert(converter:(A)->B):B? {
  return when(this) {
    null-> null
    else-> converter(this)
  }
}
