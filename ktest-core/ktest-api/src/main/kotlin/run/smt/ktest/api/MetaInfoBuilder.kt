package run.smt.ktest.api

import run.smt.ktest.api.internal.Internals
import run.smt.ktest.util.collection.partitionBy
import java.lang.annotation.Repeatable
import java.time.Duration
import kotlin.reflect.KClass
import run.smt.ktest.util.reflection.a as _a

typealias MetaInfoDSL = MetaInfoBuilder.() -> Unit

class MetaInfoBuilder internal constructor() {
    private val metaProperties = mutableSetOf<MetaProperty<*>>()

    internal fun toMetaProperties(): Set<MetaProperty<*>> {
        val (annotations, others) = metaProperties.partitionBy { it is AnnotationBasedProperty<*> }
        val processedAnnotations = normalize(annotations.map { (it as AnnotationBasedProperty<*>).value }).map { AnnotationBasedProperty(it) }
        return (processedAnnotations + others).toSet()
    }

    fun Internals.register(property: MetaProperty<*>) { metaProperties += property }

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


internal fun normalize(annotations: List<Annotation>): List<Annotation> {
    val (normalizable, alreadyNormalized) = annotations.asSequence()
        .groupBy { it::class }
        .toList()
        .partitionBy { it.first.annotations.any { it is Repeatable } && it.second.size > 1 }

    return alreadyNormalized.flatMap { it.second } +
        normalizable.map { (k, v) ->
            val wrapper = k.annotations.find { it is Repeatable } as? Repeatable
                ?: throw IllegalStateException("Can't really happen")
            _a(wrapper.value, v.toTypedArray())
        }
}

operator fun MetaInfoDSL.plus(other: MetaInfoDSL): MetaInfoDSL = {
    this@plus(this)
    other(this)
}
