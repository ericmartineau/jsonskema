package lang

actual typealias Global = JvmStatic
actual typealias Field = JvmField

actual typealias Serializable = kotlinx.serialization.Serializable
actual typealias SerializableWith = kotlinx.serialization.Serializable
actual annotation class Name actual constructor(actual val name: String)
