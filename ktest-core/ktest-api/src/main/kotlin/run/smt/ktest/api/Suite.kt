package run.smt.ktest.api

import run.smt.ktest.util.functional.Either.Either
import run.smt.ktest.util.functional.Either.left
import run.smt.ktest.util.functional.Either.right
import run.smt.ktest.util.functional.Try.Try
import kotlin.reflect.KClass

/**
 * Represents suite to be executed with all its children (both cases and other suites)
 */
class Suite private constructor(
    val name: String,
    /**
     * Parent suite, it must be either other suite or some class
     */
    val parent: Either<Suite, KClass<*>>,
    /**
     * Metadata for suite, will be appended to children metadata
     */
    val metaData: MetaData = emptySet(),
    initializer: (Suite) -> Unit
) {
    val interceptors
        get() = _interceptors.toList()

    private val _childSuites = mutableListOf<Suite>()
    val childSuites
        get() = _childSuites.toList()
    private val _childCases = mutableListOf<Case>()
    val childCases
        get() = _childCases.toList()
    private val _interceptors = mutableListOf<Interceptor>()
    private val initialization = lazy { Try.of { initializer(this) } }

    constructor(name: String, parent: Suite, metaProperties: MetaData = emptySet(), initializer: (Suite) -> Unit): this(
        name,
        left(parent),
        metaProperties,
        initializer
    )

    constructor(name: String, parent: KClass<*>, metaProperties: MetaData = emptySet(), initializer: (Suite) -> Unit): this(
        name,
        right(parent),
        metaProperties,
        initializer
    )

    val allChildSuites: List<Suite>
        get() = _childSuites.toList() + _childSuites.flatMap { it.allChildSuites }

    val allChildCases: List<Case>
        get() = _childCases.toList() + _childSuites.flatMap { it.allChildCases }

    val inheritedMetaData: MetaData
        get() = metaData + parent.unify({ it.inheritedMetaData }, { emptySet() })

    val inheritedInterceptors: List<Interceptor>
        get() = interceptors + parent.unify({ it.inheritedInterceptors }, { emptyList() })

    /**
     * Search for class where this suite was defined
     */
    val testClass : KClass<*> = parent.unify({ it.testClass }, { it })

    /**
     * Full path to this suite taking into account all its parents
     */
    val fullPath: List<String> = parent.unify({ it.fullPath }, { emptyList() }) + listOf(name)

    val fullName = fullPath.joinToString(".")

    fun initialize() = initialization.value.exception

    /**
     * Add child case
     */
    fun addCase(case: Case) {
        _childCases += case
    }

    /**
     * Add child suite
     */
    fun addSuite(suite: Suite) {
        _childSuites += suite
    }

    /**
     * Add interceptor (before, after hook)
     */
    fun addInterceptor(interceptor: Interceptor) {
        _interceptors += interceptor
    }
}
