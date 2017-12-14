package run.smt.ktest.internal.api

import run.smt.ktest.Case
import run.smt.ktest.internal.Interceptor
import run.smt.ktest.util.functional.Try.*

class Suite(
    val name: String,
    internal val annotations: List<Annotation> = emptyList(),
    initializer: (Suite) -> Unit
) {
    internal val nestedSuites = mutableListOf<Suite>()
    internal val cases = mutableListOf<Case>()
    internal val interceptors = mutableListOf<Interceptor>()
    private val initialization = lazy { Try.of { initializer(this) } }

    fun addInterceptor(interceptor: Interceptor) {
        interceptors += interceptor
    }

    fun addNestedSuite(suite: Suite) {
        nestedSuites += suite
    }

    fun addCase(case: Case) {
        cases += case
    }

    fun initialize(): Throwable? {
        return initialization.value.exception
    }
}
