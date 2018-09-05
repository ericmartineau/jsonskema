package lang

import kotlinx.serialization.KSerializer
import kotlin.reflect.KClass

actual annotation class Global
actual annotation class Field
actual annotation class Serializable
actual annotation class SerializableWith actual constructor( actual val with: KClass<out KSerializer<*>> )
actual typealias Name = JsName
