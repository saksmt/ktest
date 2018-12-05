package run.smt.ktest.api

import run.smt.ktest.api.internal.Internals
import java.lang.annotation.Repeatable
import java.time.Duration
import kotlin.reflect.KClass
import run.smt.ktest.util.reflection.a as _a

typealias MetaInfoDSL = MetaInfoBuilder.() -> Unit

class MetaInfoBuilder internal constructor() {
    private val metaProperties = mutableSetOf<MetaProperty<*>>()

    internal fun toMetaProperties(): Set<MetaProperty<*>> {
        val (annotations, others) = metaProperties.partition { it is AnnotationBasedProperty<*> }
        val processedAnnotations = annotations.map { (it as AnnotationBasedProperty<*>).value }.normalize().map { AnnotationBasedProperty(it) }
        return (processedAnnotations + others).toSet()
    }

    fun Internals.register(property: MetaProperty<*>) {
        metaProperties += property
    }

    fun annotation(value: Annotation) = Internals.register(AnnotationBasedProperty(value))
    inline fun <reified A : Annotation> a(value: Any?) = annotation(_a<A>(value))
    inline fun <reified A : Annotation> a(parameters: Map<String, Any>) = annotation(_a<A>(parameters))
    inline fun <reified A : Annotation> a(vararg parameters: Pair<String, Any>) = annotation(_a<A>(*parameters))

    fun timeout(duration: Duration) = Internals.register(TimeoutProperty(duration))
    fun disable(reason: String? = null) = Internals.register(Disabled(reason))
    fun categories(vararg categories: String) = categories.map(::CategoryProperty).forEach { Internals.register(it) }
    fun categories(vararg categories: KClass<*>) = categories(*categories.mapNotNull { it.qualifiedName }.toTypedArray())
    fun category(category: String) = categories(category)
    fun category(category: KClass<*>) = categories(category)
    inline fun <reified C : Any> category() = category(C::class)
    fun invocations(count: Int) = Internals.register(MultipleInvocationsProperty(count))
    fun threads(count: Int) = Internals.register(ThreadsProperty(count))
}

fun metaInfo(dsl: MetaInfoDSL) = MetaInfoBuilder().apply(dsl).toMetaProperties()

internal fun List<Annotation>.normalize(): List<Annotation> = groupBy { it::class }.flatMap { (clazz, annotations) ->
    annotations.takeIf { size > 1 }?.let { combine(clazz, it) }?.let { listOf(it) } ?: annotations
}

internal fun combine(clazz: KClass<out Annotation>, annotations: List<Annotation>): Annotation? {
    return clazz.annotations.filterIsInstance<Repeatable>().firstOrNull()?.value?.let { _a(it, annotations.toTypedArray()) }
}

operator fun MetaInfoDSL.plus(other: MetaInfoDSL): MetaInfoDSL = {
    this@plus()
    other()
}
