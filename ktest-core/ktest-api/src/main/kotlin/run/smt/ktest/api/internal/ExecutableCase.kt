package run.smt.ktest.api.internal

import run.smt.ktest.api.Case
import run.smt.ktest.api.Interceptor
import run.smt.ktest.api.execAfter
import run.smt.ktest.api.execBefore
import run.smt.ktest.api.lifecycle.LifecycleNotifier
import java.util.concurrent.*
import kotlinx.coroutines.experimental.*
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.system.measureTimeMillis

private enum class TestRunResult {
    SUCCEED, FAILED, SKIPPED
}

class ExecutableCase internal constructor(val case: Case, private val notifier: LifecycleNotifier) {
    fun execute() {
        val interceptors = case.suite.inheritedInterceptors

        if (case.enabled) {
            if (case.invocations > 2) {
                runAll(interceptors)
            } else {
                runBlocking {
                    runCase(
                        createExecutor().asCoroutineDispatcher(),
                        notifier::emitCaseStart,
                        notifier::emitCaseSuccess,
                        notifier::emitCaseFailure,
                        notifier::emitCaseFinished,
                        notifier::emitCaseSkipped,
                        interceptors,
                        case.timeout.toMillis()
                    )
                }
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

    private suspend fun runCase(ec: CoroutineContext,
                                start: (Case) -> Unit,
                                success: (Case) -> Unit,
                                failure: (Throwable, Case) -> Unit,
                                finish: (Case) -> Unit,
                                skip: (Throwable, Case) -> Unit,
                                interceptors: List<Interceptor>,
                                timeoutMs: Long): TestRunResult {
        val beforeTook = try {
            timedOut(ec, timeoutMs) {
                try {
                    measureTimeMillis { interceptors.execBefore() }
                } catch (e: Throwable) {
                    throw PhaseExecutionException(e)
                }
            }
        } catch (e: TimeoutCancellationException) {
            failure(timeoutException(e, timeoutMs, timeoutMs, "before"), case)
            return TestRunResult.FAILED
        } catch (e: PhaseExecutionException) {
            skip(e.exception, case)
            return TestRunResult.SKIPPED
        }

        val timeoutForBodyAndAfter = timeoutMs - beforeTook
        val bodyTook = try {
            start(case)
            timedOut(ec, timeoutForBodyAndAfter) {
                try {
                    measureTimeMillis { case.body() }
                } catch (e: Throwable) {
                    throw PhaseExecutionException(e)
                }
            }
        } catch (e: TimeoutCancellationException) {
            failure(timeoutException(e, timeoutMs, timeoutForBodyAndAfter, "test body"), case)
            return TestRunResult.FAILED
        } catch (e: PhaseExecutionException) {
            failure(e.exception, case)
            return TestRunResult.FAILED
        }

        val timeoutForAfter = timeoutForBodyAndAfter - bodyTook

        try {
            timedOut(ec, timeoutForAfter) {
                try {
                    interceptors.execAfter()
                } catch (e: Throwable) {
                    throw PhaseExecutionException(e)
                }
            }
        } catch (e: TimeoutCancellationException) {
            failure(timeoutException(e, timeoutMs, timeoutForAfter, "after"), case)
            return TestRunResult.FAILED
        } catch (e: PhaseExecutionException) {
            failure(e.exception, case)
            return TestRunResult.FAILED
        }
        success(case)
        finish(case)
        return TestRunResult.SUCCEED
    }

    private fun runAll(interceptors: List<Interceptor>) {
        val ec = createExecutor().asCoroutineDispatcher()
        val results = (1..case.invocations)
            .map { runNumber -> runNumber to launch { runCase(
                ec = CoroutineName("${case.name}#$runNumber") + ec,
                start = { notifier.emitCaseRunStart(it, runNumber) },
                success = { notifier.emitCaseRunSuccess(it, runNumber) },
                failure = { e, case -> notifier.emitCaseRunFailure(e, case, runNumber) },
                finish = { notifier.emitCaseRunFinished(it, runNumber) },
                skip = { e, case -> notifier.emitCaseRunSkipped(e, case, runNumber) },
                interceptors = interceptors,
                timeoutMs = case.timeout.toMillis()
            ) } }

        val timeout = case.timeout.toMillis() + (case.timeout.toMillis() * 0.1).toLong()
        runBlocking { try {
            // awaiting for all child jobs to complete with timeout equal to every job timeout plus 10%
            timedOut(ec, timeout) {
                results.forEach { it.second.join() }
                notifier.emitCaseSuccess(case)
                notifier.emitCaseFinished(case)
            }
        } catch (e: TimeoutCancellationException) {
            notifier.emitCaseFailure(timeoutException(e, timeout, timeout, "whole test"), case)
        } }
    }

    private fun createExecutor(): ExecutorService {
        return if (case.threads < 2) {
            Executors.newSingleThreadExecutor()
        } else {
            Executors.newFixedThreadPool(case.threads + 1)
        }
    }

    private fun timeoutException(e: TimeoutCancellationException, originalTimeout: Long, timeout: Long, phase: String): TimeoutException {
        val ex = TimeoutException("Test aborted due to timeout exceeded on phase \"$phase\" with original timeout=${originalTimeout}ms and phase timeout=${timeout}ms")
        ex.initCause(e)
        return ex
    }

    private class PhaseExecutionException(val exception: Throwable) : Exception()

    private suspend fun <R> timedOut(ec: CoroutineContext, timeout: Long, action: suspend () -> R): R {
        val deferred = async(ec) {
            action()
        }
        return withTimeout(timeout, TimeUnit.MILLISECONDS) { deferred.await() }
    }
}
