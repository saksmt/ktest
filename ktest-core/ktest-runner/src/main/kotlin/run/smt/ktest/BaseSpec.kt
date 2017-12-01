package run.smt.ktest

import run.smt.ktest.internal.*
import run.smt.ktest.internal.api.SpecBuilder
import run.smt.ktest.internal.api.Suite
import run.smt.ktest.internal.util.sanitizeSpecName
import java.io.Closeable
import java.util.*

abstract class BaseSpec {
    internal var currentSuite = Suite(javaClass.simpleName) {}
        private set(value) {
            field = value
        }

    protected open val defaultTestCaseConfig: TestCaseConfig = TestCaseConfig()
    private val closeablesInReverseOrder = LinkedList<Closeable>()

    fun <T : Closeable> autoClose(closeable: T): T {
        closeablesInReverseOrder.addFirst(closeable)
        return closeable
    }

    fun before(body: () -> Unit) = SpecBuilder.addBeforeEachHook(body)
    fun after(body: () -> Unit) = SpecBuilder.addAfterEachHook(body)

    fun beforeAll(body: () -> Unit) = SpecBuilder.addBeforeAllHook(body)
    fun afterAll(body: () -> Unit) = SpecBuilder.addAfterAllHook(body)

    internal fun closeResources(exceptionHandler: (AssertionError) -> Unit) {
        closeablesInReverseOrder.forEach {
            try {
                it.close()
            } catch (exception: AssertionError) {
                exceptionHandler(exception)
            }
        }
    }

    protected fun SpecBuilder.addBeforeAllHook(body: () -> Unit) {
        synchronized(this) {
            currentSuite.addInterceptor(Interceptor(before = executeOnce(body)))
        }
    }

    protected fun SpecBuilder.addAfterAllHook(body: () -> Unit) {
        synchronized(this) {
            currentSuite.addInterceptor(Interceptor(after = executeOnce(body)))
        }
    }

    protected fun SpecBuilder.addAfterEachHook(body: () -> Unit) {
        synchronized(this) {
            currentSuite.addInterceptor(Interceptor(before = executeEveryTime(body)))
        }
    }

    protected fun SpecBuilder.addBeforeEachHook(body: () -> Unit) {
        synchronized(this) {
            currentSuite.addInterceptor(Interceptor(after = executeEveryTime(body)))
        }
    }

    protected fun <T> SpecBuilder.suite(name: String, body: () -> T) {
        synchronized(this) {
            currentSuite.addNestedSuite(Suite(sanitizeSpecName(name)) {
                synchronized(this) {
                    val parentSuite = currentSuite
                    currentSuite = it
                    body()
                    currentSuite = parentSuite
                }
            })
        }
    }

    protected fun SpecBuilder.case(name: String, annotations: List<Annotation>, body: () -> Unit): Case {
        return synchronized(this) {
            Case(currentSuite, sanitizeSpecName(name), body, defaultTestCaseConfig, annotations)
                .also(currentSuite::addCase)
        }
    }
}
