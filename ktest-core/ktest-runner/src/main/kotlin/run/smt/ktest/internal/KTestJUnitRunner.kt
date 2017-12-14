package run.smt.ktest.internal

import org.junit.Ignore
import org.junit.runner.Description
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunNotifier
import org.junit.runners.ParentRunner
import org.junit.runners.model.TestTimedOutException
import run.smt.ktest.BaseSpec
import run.smt.ktest.Case
import run.smt.ktest.TestCaseConfig
import run.smt.ktest.internal.api.Suite
import run.smt.ktest.util.reflection.a
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class KTestJUnitRunner(testClass: Class<BaseSpec>) : ParentRunner<ExecutableCase>(testClass) {

    private val instance = testClass.newInstance().also { initialize(it.currentSuite) }

    override fun getName() = instance.javaClass.simpleName!!

    override fun getDescription() = describe(instance.currentSuite)

    override fun getChildren() = cases(instance.currentSuite)

    private fun initialize(suite: Suite) {
        val initializationException = suite.initialize()
        if (initializationException != null) {
            // that's a hack! :)
            suite.addCase(Case(suite, "initialization", {
                throw initializationException
            }, TestCaseConfig()))
        }
        suite.nestedSuites.forEach(this::initialize)
    }

    private fun describe(suite: Suite): Description {
        val description = Description.createSuiteDescription(suite.name, emptyArray<Annotation>())
        suite.nestedSuites.forEach {
            description.addChild(describe(it))
        }
        suite.cases.forEach {
            description.addChild(it.description)
        }
        return description
    }

    private fun cases(suite: Suite): List<ExecutableCase> {
        val allCases = suite.nestedSuites.flatMap { cases(it) } +
            suite.cases.map { ExecutableCase(it) }

        return allCases.map { (i: List<Interceptor>, c: Case) ->
            ExecutableCase(suite.interceptors.toList() + i, c)
        }
    }

    override fun getRunnerAnnotations() = emptyArray<Annotation>()

    override fun run(notifier: RunNotifier?) {
        notifier ?: return

        super.run(notifier)

        instance.closeResources {
            notifier.fireTestFailure(Failure(description, it))
        }
    }

    override fun describeChild(child: ExecutableCase) = child.case.description

    override fun runChild(child: ExecutableCase?, notifier: RunNotifier?) {
        notifier ?: return
        val executable = child ?: return
        val case = executable.case

        if (case.isActive) {
            notifier.fireTestStarted(case.description)
            executeTest(executable, notifier)
            notifier.fireTestFinished(case.description)
        } else {
            val annotations = case.config.disablingReason?.let {
                if (case.annotations.any { it is Ignore }) {
                    case.annotations + a<Ignore>(case.config.disablingReason)
                } else {
                    null
                }
            } ?: case.annotations
            notifier.fireTestIgnored(case.copy(annotations = annotations).description)
        }
    }

    private fun executeTest(executable: ExecutableCase, notifier: RunNotifier) {
        val case = executable.case
        val executor = createExecutor(case)

        val results = (1..case.config.invocations).asSequence()
            .map { createTestRun(executable) }
            .map { executor.submit(it) }
            .toList()

        executor.shutdown()
        val timeout = case.config.timeout
        val terminated = executor.awaitTermination(timeout.toNanos(), TimeUnit.NANOSECONDS)

        results.forEach {
            if (!it.isDone) {
                notifier.fireTestFailure(Failure(
                    case.description,
                    TestTimedOutException(timeout.toNanos(), TimeUnit.NANOSECONDS)
                ))
            } else {
                val result = it.get()
                if (result is Failure) {
                    notifier.fireTestFailure(result)
                }
            }
        }

        if (!terminated) {
            val failure = Failure(description, TestTimedOutException(timeout.toNanos(), TimeUnit.NANOSECONDS))
            notifier.fireTestFailure(failure)
        }
    }

    private fun createTestRun(executable: ExecutableCase): Callable<Any> {
        return Callable {
            try {
                executable.execute()
            } catch (exception: Throwable) {
                Failure(executable.case.description, exception)
            }
        }
    }

    private fun createExecutor(case: Case): ExecutorService {
        return if (case.config.threads < 2) {
            Executors.newSingleThreadExecutor()
        } else {
            Executors.newFixedThreadPool(case.config.threads)
        }
    }
}
