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
    internal var running = false

    protected open val defaultTestCaseConfig: TestCaseConfig = TestCaseConfig()
    private val closeablesInReverseOrder = LinkedList<Closeable>()

    fun <T : Closeable> autoClose(closeable: T): T {
        return modify {
            closeablesInReverseOrder.addFirst(closeable)
            closeable
        }
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

    fun SpecBuilder.addBeforeAllHook(body: () -> Unit) {
        modify {
            currentSuite.addInterceptor(Interceptor(before = executeOnce(body)))
        }
    }

    fun SpecBuilder.addAfterAllHook(body: () -> Unit) {
        modify {
            currentSuite.addInterceptor(Interceptor(after = executeOnce(body)))
        }
    }

    fun SpecBuilder.addAfterEachHook(body: () -> Unit) {
        modify {
            currentSuite.addInterceptor(Interceptor(before = executeEveryTime(body)))
        }
    }

    fun SpecBuilder.addBeforeEachHook(body: () -> Unit) {
        modify {
            currentSuite.addInterceptor(Interceptor(after = executeEveryTime(body)))
        }
    }

    fun <T> SpecBuilder.suite(name: String, body: () -> T) = suite(name, emptyList(), body)
    fun <T> SpecBuilder.suite(name: String, annotations: List<Annotation>, body: () -> T) {
        modify {
            currentSuite.addNestedSuite(Suite(sanitizeSpecName(name), annotations, currentSuite) {
                synchronized(this) {
                    val parentSuite = currentSuite
                    currentSuite = it
                    body()
                    currentSuite = parentSuite
                }
            })
        }
    }

    fun SpecBuilder.case(name: String, annotations: List<Annotation>, body: () -> Unit): Case {
        return modify {
            Case(currentSuite, sanitizeSpecName(name), body, defaultTestCaseConfig, annotations)
                .also(currentSuite::addCase)
        }
    }

    private fun <T> modify(modifier: () -> T) = synchronized(this) {
        if (running) {
            throw IllegalStateException("Spec can not be modified while it is running! Usage of RestTest may cause this error if used inside test-case!")
        }
        modifier()
    }
}
