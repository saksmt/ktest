package run.smt.ktest

import org.junit.runner.Description
import org.junit.runner.RunWith
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunNotifier
import org.junit.runners.model.TestTimedOutException
import java.io.Closeable
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import io.kotlintest.util.a as a_

@RunWith(KTestJUnitRunner::class)
abstract class Spec {

  companion object {
    inline fun <reified T : Annotation> a(valueArg: Any?): T = a_(valueArg)
    inline fun <reified T : Annotation> a(args: Map<String, Any?>): T = a_(args)
    inline fun <reified T : Annotation> a(vararg args: Pair<String, Any?> = emptyArray()): T = a_(*args)
  }

  // the root test suite which uses the simple name of the class as the name of the suite
  // spec implementations will add their tests to this suite
  protected val rootTestSuite = TestSuite(javaClass.simpleName)

  /**
   * Read-only list of all test cases of this spec for use in interceptors.
   */
  val testCases: List<TestCase> = rootTestSuite.testCases

  protected open val oneInstancePerTest = true
  internal val testContext: Spec
    get() { return if (oneInstancePerTest) javaClass.newInstance() else this }

  // returns a jUnit Description for the currently registered tests
  internal fun description(): Description = rootTestSuite.description()

  /**
   * Config applied to each test case if not overridden per test case.
   */
  protected open val defaultTestCaseConfig: TestCaseConfig = TestCaseConfig()

  /**
   * Interceptors that intercepts the execution of the whole spec. Interceptors are executed from
   * left to right.
   */
  protected open val specInterceptors: List<(Spec, () -> Unit) -> Unit> = listOf()

  internal val allSpecInterceptors: List<(Spec, () -> Unit) -> Unit>
    get() { return listOf(this::interceptSpec) + specInterceptors }

  internal val testCaseInterceptor: (TestCaseContext, () -> Unit) -> Unit
    get() { return this::interceptTestCase }

  internal val closeablesInReverseOrder = LinkedList<Closeable>()

  internal val children: List<TestCase> by lazy { rootTestSuite.testCasesIncludingChildren() }

  /**
   * Registers a field for auto closing after all tests have run.
   */
  protected fun <T : Closeable> autoClose(closeable: T): T {
    closeablesInReverseOrder.addFirst(closeable)
    return closeable
  }

  /**
   * Intercepts the call of each test case.
   *
   * Don't forget to call `test()` in the body of this method. Otherwise the test case will never be
   * executed.
   */
  protected open fun interceptTestCase(context: TestCaseContext, test: () -> Unit) {
    test()
  }

  /**
   * Intercepts the call of whole spec.
   *
   * Don't forget to call `spec()` in the body of this method. Otherwise the spec will never be
   * executed.
   */
  protected open fun interceptSpec(context: Spec, spec: () -> Unit) {
    spec()
  }

  internal fun closeResources(notifier: RunNotifier) {
    closeablesInReverseOrder.forEach {
      try {
        it.close()
      } catch(exception: AssertionError) {
        notifier.fireTestFailure(Failure(description(), exception))
      }
    }
  }
}