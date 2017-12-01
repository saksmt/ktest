package run.smt.ktest

import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunNotifier
import org.junit.runners.ParentRunner
import org.junit.runners.model.TestTimedOutException
import java.util.ArrayList
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future

class KTestJUnitRunner(testClass: Class<Spec>) : ParentRunner<Int>(testClass) {

  private val instance = testClass.newInstance()

  override fun getName() = instance.javaClass.simpleName!!

  override fun getChildren(): List<Int> {
    return (0 until instance.children.size).toList()
  }

  override fun getDescription() = instance.description()

  override fun run(notifier: RunNotifier?) {
    notifier ?: return

    Project.beforeAll()
    val interceptorChain = createInterceptorChain(instance.allSpecInterceptors) { _, testCase -> testCase() }

    interceptorChain(instance) {
      super.run(notifier)
    }

    instance.closeResources(notifier)
    Project.afterAll()
  }

  override fun describeChild(child: Int?) = child?.let { instance.children[child].description }

  override fun runChild(child: Int?, notifier: RunNotifier?) {
    notifier ?: return
    val index = child ?: return
    val context = instance.testContext
    val actualCase = context.children[index]

    if (!actualCase.isActive) {
      notifier.fireTestIgnored(actualCase.description)
      return
    }

    val executor =
        if (actualCase.config.threads < 2) Executors.newSingleThreadExecutor()
        else Executors.newFixedThreadPool(actualCase.config.threads)
    notifier.fireTestStarted(actualCase.description)
    val initialInterceptor = { context: TestCaseContext, testCase: () -> Unit ->
       context.spec.testCaseInterceptor(context, { testCase() })
    }
    val interceptorChain = createInterceptorChain(actualCase.config.interceptors, initialInterceptor)
    val testCaseContext = TestCaseContext(context, actualCase)
    val results = ArrayList<Future<Any>>()
    for (j in 1..actualCase.config.invocations) {
      val callable = Callable {
        try {
          interceptorChain(testCaseContext, { actualCase.test() })
        } catch (exception: Throwable) {
          Failure(actualCase.description, exception)
        }
      }
      results.add(executor.submit(callable))
    }
    executor.shutdown()
    val timeout = actualCase.config.timeout
    val terminated = executor.awaitTermination(timeout.amount, timeout.timeUnit)
    results.forEach {
      val result = it.get()
      if (result is Failure) {
        notifier.fireTestFailure(result)
      }
    }
    if (!terminated) {
      val failure = Failure(description, TestTimedOutException(timeout.amount, timeout.timeUnit))
      notifier.fireTestFailure(failure)
    }
    notifier.fireTestFinished(actualCase.description)
  }

  private fun <CONTEXT> createInterceptorChain(
      interceptors: Iterable<(CONTEXT, () -> Unit) -> Unit>,
      initialInterceptor: (CONTEXT, () -> Unit) -> Unit): (CONTEXT, () -> Unit) -> Unit {
    return interceptors.reversed().fold(initialInterceptor) { a, b ->
      {
        context: CONTEXT, testCase: () -> Unit -> b(context, { a.invoke(context, { testCase() }) })
      }
    }
  }
}