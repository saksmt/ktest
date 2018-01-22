package run.smt.ktest.api.internal

import run.smt.ktest.api.Case
import run.smt.ktest.api.Interceptor
import run.smt.ktest.api.execAfter
import run.smt.ktest.api.execBefore
import run.smt.ktest.api.lifecycle.LifecycleNotifier
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

private enum class TestRunResult {
    SUCCEED, FAILED, SKIPPED
}

class ExecutableCase internal constructor(val case: Case, private val notifier: LifecycleNotifier) {
    fun execute() {
        val interceptors = case.suite.inheritedInterceptors

        if (case.enabled) {
            if (case.invocations > 2) {
                executeAllRuns(interceptors)
            } else {
                runCase(
                    notifier::emitCaseStart,
                    notifier::emitCaseSuccess,
                    notifier::emitCaseFailure,
                    notifier::emitCaseFinished,
                    notifier::emitCaseSkipped,
                    interceptors
                )
            }
        } else {
            notifier.emitCaseIgnored(case)
        }
    }

    private fun runCase(
        start: (Case) -> Unit,
        success: (Case) -> Unit,
        failure: (Throwable, Case) -> Unit,
        finish: (Case) -> Unit,
        skip: (Throwable, Case) -> Unit,
        interceptors: List<Interceptor>
    ): TestRunResult {
        try {
            interceptors.execBefore()
        } catch (e: Throwable) {
            skip(e, case)
            return TestRunResult.SKIPPED
        }
        start(case)
        val result = try {
            case.body()
            interceptors.execAfter()
            success(case)
            TestRunResult.SUCCEED
        } catch (e: Throwable) {
            failure(e, case)
            TestRunResult.FAILED
        }
        finish(case)
        return result
    }

    private fun executeAllRuns(interceptors: List<Interceptor>) {
        val executor = createExecutor()

        val results = (1..case.invocations).asSequence()
            .map { runNumber -> runNumber to Callable {
                runCase(
                    start = { notifier.emitCaseRunStart(it, runNumber) },
                    success = { notifier.emitCaseRunSuccess(it, runNumber) },
                    failure = { e, case -> notifier.emitCaseRunFailure(e, case, runNumber) },
                    finish = { notifier.emitCaseRunFinished(it, runNumber) },
                    skip = { e, case -> notifier.emitCaseRunSkipped(e, case, runNumber) },
                    interceptors = interceptors
                )
            } }
            .map { it.first to executor.submit(it.second) }
            .toList()

        val terminated = executor.awaitTermination(case.timeout.toNanos(), TimeUnit.NANOSECONDS)

        results.forEach { (runNumber, it) ->
            if (!it.isDone) {
                notifier.emitCaseRunFailure(TimeoutException(), case, runNumber)
            }
        }

        if (terminated) {
            notifier.emitCaseSuccess(case)
        } else {
            notifier.emitCaseFailure(TimeoutException(), case)
        }
    }

    private fun createExecutor(): ExecutorService {
        return if (case.threads < 2) {
            Executors.newSingleThreadExecutor()
        } else {
            Executors.newFixedThreadPool(case.threads)
        }
    }
}
