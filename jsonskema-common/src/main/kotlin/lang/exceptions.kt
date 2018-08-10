package lang

fun illegalState(message:String? = null):Nothing {
  throw IllegalStateException(message)
}
