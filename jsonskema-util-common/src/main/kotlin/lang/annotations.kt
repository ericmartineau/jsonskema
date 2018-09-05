package lang

import kotlinx.serialization.KSerializer
import kotlin.annotation.AnnotationTarget.*
import kotlin.reflect.KClass

expect annotation class Global()
expect annotation class Field()

@Retention(AnnotationRetention.BINARY)
@Target(CLASS, FUNCTION, PROPERTY, CONSTRUCTOR, PROPERTY_GETTER, PROPERTY_SETTER)
expect annotation class Name(val name:String)

@Target( PROPERTY, CLASS )
expect annotation class Serializable()

@Target( PROPERTY, CLASS )
expect annotation class SerializableWith( val with: KClass<out KSerializer<*>>)
