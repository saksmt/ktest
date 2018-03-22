package run.smt.ktest.api.internal

import run.smt.ktest.api.*
import run.smt.ktest.api.lifecycle.Lifecycle

enum class InitializationMode {
    EAGER, LAZY
}

class SpecExecutor<R: RunnerDescription>(
    specClass: Class<out BaseSpec>,
    initializationMode: InitializationMode,
    runnerDescription: R
) {
    private val spec = instantiateSpec(specClass)

    val rootSuite by lazy { with(spec) { Internals.currentSuite.also { initialize(it) } } }
    private val notifier by lazy { Lifecycle.createNotifierFor(spec) }

    init {
        with(spec) {
            Internals.runnerDescription = runnerDescription
        }

        if (initializationMode == InitializationMode.EAGER) {
            rootSuite.name // initializing
        }
    }

    val executables: List<ExecutableCase> by lazy {
        rootSuite.allChildCases.map { ExecutableCase(it, notifier) }
    }

    fun startup() {
        with(spec) {
            Internals.initialize {
                notifier.emitCaseFailure(it, Case(rootSuite, "initialization", rootSuite.inheritedMetaData) {})
            }
        }
    }

    fun finalize() {
        with(spec) {
            Internals.finalize {
                notifier.emitCaseFailure(
                    it,
                    Case(rootSuite, "finalization", rootSuite.inheritedMetaData) {}
                )
            }
        }
    }

    private fun initialize(suite: Suite) {
        val initializationException = suite.initialize()
        if (initializationException != null) {
            // that's a hack! :)
            suite.addCase(Case(suite, "initialization", noMetaData()) {
                throw initializationException
            })
        }
        suite.childSuites.forEach(this::initialize)
    }

    private fun instantiateSpec(specClass: Class<out BaseSpec>): BaseSpec {
        return specClass.kotlin.objectInstance ?: specClass.newInstance()
    }
}
