package run.smt.ktest.api

import java.time.Duration

typealias MetaData = Set<MetaProperty<*>>

fun emptyMetaData(): MetaData = emptySet()
fun noMetaData() = emptyMetaData()

abstract class MetaProperty<out T> {
    abstract val value: T
}

data class NamedMetaProperty<out T>(val name: String, override val value: T) : MetaProperty<T>()

data class MultipleInvocationsProperty(override val value: Int) : MetaProperty<Int>()

data class Disabled(override val value: String?): MetaProperty<String?>() {
    constructor(): this(null)
}

data class AnnotationBasedProperty<out A: Annotation>(override val value : A) : MetaProperty<A>() {
    constructor(convertible: ConvertibleToAnnotation<A>): this(convertible.toAnnotation())
}

data class TimeoutProperty(override val value: Duration): MetaProperty<Duration>()

data class ThreadsProperty(override val value: Int): MetaProperty<Int>()

data class CategoryProperty(override val value: String): MetaProperty<String>()

interface ConvertibleToAnnotation<out A: Annotation> {
    fun toAnnotation(): A
}
