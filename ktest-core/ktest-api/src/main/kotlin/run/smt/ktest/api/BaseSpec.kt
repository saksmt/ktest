package run.smt.ktest.api

import run.smt.ktest.api.internal.Internals
import run.smt.ktest.api.internal.SpecBuilder
import java.util.*
import run.smt.ktest.util.reflection.a as _a
import run.smt.ktest.api.metaInfo as buildMetaInfo

abstract class BaseSpec {

    private var currentSuite = Suite(javaClass.simpleName, this::class) {}
    val Internals.currentSuite
        get() = this@BaseSpec.currentSuite

    private var runnerDescription: RunnerDescription? = null
    var Internals.runnerDescription
        get() = this@BaseSpec.runnerDescription
        set(value) { this@BaseSpec.runnerDescription = value }

    private var frozen = false
    var Internals.frozen
        get() = this@BaseSpec.frozen
        set(value) { this@BaseSpec.frozen = value }

    private val closeablesInReverseOrder = LinkedList<AutoCloseable>()

    fun <T : AutoCloseable> autoClose(closeable: T): T {
        return modify {
            closeablesInReverseOrder.addFirst(closeable)
            closeable
        }
    }

    fun before(body: () -> Unit) = SpecBuilder.addBeforeEachHook(body)
    fun after(body: () -> Unit) = SpecBuilder.addAfterEachHook(body)

    fun beforeAll(body: () -> Unit) = SpecBuilder.addBeforeAllHook(body)
    fun afterAll(body: () -> Unit) = SpecBuilder.addAfterAllHook(body)

    fun Internals.closeResources(exceptionHandler: (AssertionError) -> Unit) {
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
            currentSuite.addInterceptor(Interceptor(after = executeEveryTime(body)))
        }
    }

    fun SpecBuilder.addBeforeEachHook(body: () -> Unit) {
        modify {
            currentSuite.addInterceptor(Interceptor(before = executeEveryTime(body)))
        }
    }

    fun <T> SpecBuilder.suite(name: String, body: () -> T) = suite(name, noMetaData(), body)
    fun <T> SpecBuilder.suite(name: String, metaInfo: MetaInfoDSL, body: () -> T) = suite(name, buildMetaInfo(metaInfo), body)
    fun <T> SpecBuilder.suite(name: String, meta: MetaData, body: () -> T) {
        modify {
            currentSuite.addSuite(Suite(name, currentSuite, meta) {
                synchronized(this@BaseSpec) {
                    val parentSuite = currentSuite
                    currentSuite = it
                    body()
                    currentSuite = parentSuite
                }
            })
        }
    }

    fun SpecBuilder.case(name: String, metaInfo: MetaInfoDSL, body: () -> Unit) = case(name, buildMetaInfo(metaInfo), body)
    fun SpecBuilder.case(name: String, body: () -> Unit) = case(name, noMetaData(), body)
    fun SpecBuilder.case(name: String, meta: MetaData, body: () -> Unit) {
        modify {
            Case(currentSuite, name, meta, body)
                .also(currentSuite::addCase)
        }
    }

    private fun <T> modify(modifier: () -> T) = synchronized(this) {
        if (frozen) {
            throw IllegalStateException(
                "${runnerDescription?.name ?: "Unknown runner"} don't support modification of running specification! " +
                    "Usage of RestTest may cause this error if used inside test-case!"
            )
        }
        modifier()
    }
}
