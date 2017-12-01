package run.smt.ktest.internal

import run.smt.ktest.Case

data class ExecutableCase(val interceptors: List<Interceptor>, val case: Case) {
    constructor(case: Case) : this(emptyList(), case)

    fun execute() {
        interceptors.execBefore()
        case.test()
        interceptors.execAfter()
    }
}
