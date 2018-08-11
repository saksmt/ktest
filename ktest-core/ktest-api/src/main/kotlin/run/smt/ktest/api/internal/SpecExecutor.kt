package run.smt.ktest.api.internal

import run.smt.ktest.api.*
import run.smt.ktest.api.lifecycle.Lifecycle
import run.smt.ktest.util.loader.instantiate

/**
 * Tells how spec should be initialized: eagerly (all before blocks executed immediately) or lazily (all blocks are
 * executed at almost the same time)
 */
enum class InitializationMode {
    EAGER, LAZY
}

/**
 * Instantiates and executes whole spec
 */
class SpecExecutor<R: RunnerDescription>(
    specClass: Class<out BaseSpec>,
    initializationMode: InitializationMode,
    runnerDescription: R
) {
    private val spec = instantiate<BaseSpec>()(specClass.kotlin)

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

    /**
     * Actual tests in spec as [ExecutableCase]s
     */
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
}
