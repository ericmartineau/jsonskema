package lang

actual typealias UUID = java.util.UUID

actual fun randomUUID(): UUID {
  return UUID.randomUUID()
}
