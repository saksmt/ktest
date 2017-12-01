package run.smt.ktest.jsonpath.subtree

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.Filter
import run.smt.ktest.jsonpath.get

typealias SubtreeDSL = SubtreeSpecBuilder.() -> Unit
typealias SubtreeSpec = Set<JsonPathSpec>

data class JsonPathSpec internal constructor(internal val path: String?, internal val filter: Filter?) {
    internal constructor(path: String) : this(path, null)
    internal constructor(filter: Filter) : this(null, filter)

    internal fun applyOn(dc: DocumentContext): DocumentContext {
        val path = this.path ?: "$..*[?]"
        return if (filter == null) dc[path] else dc[path, filter]
    }
}

abstract class AbstractSubtreeBuilder<S: AbstractSubtreeBuilder<S, C>, C> internal constructor(private val basePath: String) {
    private val children: MutableSet<C> = mutableSetOf()
    internal val collected: Set<C> get() {
        return children.map { it.prependBasePath(basePath) }.toSet()
    }

    operator fun String.invoke(dsl: S.() -> Unit): S {
        val newSubtree = newInstance("." + this)
        newSubtree.apply(dsl)

        children += newSubtree.collected

        return self()
    }

    protected fun addValue(value: C) {
        children += value.prependToPath(".")
    }

    protected abstract fun C.prependToPath(what: String): C
    protected abstract fun C.prependBasePath(basePath: String): C
    protected abstract fun newInstance(newBasePath: String): S
    protected abstract fun self(): S
}

internal fun <E, B: AbstractSubtreeBuilder<B, E>> createSubtreeCreator(builderFactory: (String) -> B): (B.() -> Unit) -> Set<E> = { dsl ->
    builderFactory("$").apply(dsl).collected
}

class SubtreeSpecBuilder internal constructor(basePath: String) : AbstractSubtreeBuilder<SubtreeSpecBuilder, JsonPathSpec>(basePath) {
    operator fun String.unaryPlus(): SubtreeSpecBuilder {
        addValue(JsonPathSpec(this))
        return self()
    }

    operator fun Filter.unaryPlus(): SubtreeSpecBuilder {
        addValue(JsonPathSpec(this))
        return self()
    }

    operator fun unaryPlus(): SubtreeSpecBuilder {
        // only for consistency of DSL
        return this
    }

    override fun JsonPathSpec.prependToPath(what: String): JsonPathSpec {
        if (path != null) {
            return copy(path = what + path)
        }
        return this
    }

    override fun JsonPathSpec.prependBasePath(basePath: String): JsonPathSpec {
        val path = this.path ?: "..*[?]"
        return copy(path = basePath + path)
    }

    override fun newInstance(newBasePath: String) = SubtreeSpecBuilder(newBasePath)
    override fun self(): SubtreeSpecBuilder = this
}

val createSubtree = createSubtreeCreator(::SubtreeSpecBuilder)
