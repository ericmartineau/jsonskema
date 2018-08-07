package lang

actual data class UUID(val juuid:java.util.UUID) {
  override fun toString(): String {
    return juuid.toString()
  }

  actual companion object {
    actual fun randomUUID(): UUID {
      return UUID(java.util.UUID.randomUUID())
    }
  }
}
