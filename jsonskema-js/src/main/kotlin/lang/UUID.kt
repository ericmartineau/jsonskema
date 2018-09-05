package lang

actual class UUID

actual fun randomUUID(): UUID {
  return UUID()
}
