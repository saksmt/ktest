package run.smt.ktest.util.reflection

import kotlin.reflect.KClass

inline fun <reified R : Any> Class<*>.canBeAssignedTo() = this canBeAssignedTo R::class

infix fun Class<*>.canBeAssignedTo(other: KClass<*>) = other.java.isAssignableFrom(this) || other.javaObjectType.isAssignableFrom(this)

inline fun <reified R : Any> KClass<*>.canBeAssignedTo() = this canBeAssignedTo R::class

infix fun <R> KClass<*>.canBeAssignedTo(other: Class<R>): Boolean {
  return other.isAssignableFrom(this.java)
      || other.isAssignableFrom(this.javaObjectType)
}

infix fun KClass<*>.canBeAssignedTo(other: KClass<*>): Boolean {
  return other.java.isAssignableFrom(this.java)
      || other.java.isAssignableFrom(this.javaObjectType)
      || other.javaObjectType.isAssignableFrom(this.java)
      || other.javaObjectType.isAssignableFrom(this.javaObjectType)
}
