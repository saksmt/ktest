package run.smt.ktest.api

import run.smt.ktest.api.internal.Internals
import run.smt.ktest.api.internal.SpecBuilder
import java.util.*
import run.smt.ktest.util.reflection.a as _a
import run.smt.ktest.api.metaInfo as buildMetaInfo

typealias Hook = ((AssertionError) -> Unit) -> Unit

/**
 * Base class for all specs
 *
 * Accumulates tests and suites using [[SpecBuilder.suite]] and [[SpecBuilder.case]] in stack like manner
 */
abstract class BaseSpec {

    private var currentSuite = Suite(javaClass.simpleName, this::class) {}
    /**
     * Current suite of this spec
     */
    val Internals.currentSuite
        get() = this@BaseSpec.currentSuite

    private var runnerDescription: RunnerDescription? = null
    /**
     * Contains description for current runner
     */
    var Internals.runnerDescription
        get() = this@BaseSpec.runnerDescription
        set(value) { this@BaseSpec.runnerDescription = value }

    private var frozen = false
    /**
     * Tells whether new cases/suites can be added to this spec
     */
    var Internals.frozen
        get() = this@BaseSpec.frozen
        set(value) { this@BaseSpec.frozen = value }

    private val initializers = mutableListOf<Hook>()
    private val finalizers = mutableListOf<Hook>(::closeResources)
    private val closeablesInReverseOrder = LinkedList<AutoCloseable>()

    /**
     * Registers automatically closable resource
     */
    fun <T : AutoCloseable> autoClose(closeable: T): T {
        return modify {
            closeablesInReverseOrder.addFirst(closeable)
            closeable
        }
    }

    /**
     * Registers new "before" hook, which will be executed before each test
     */
    fun before(body: () -> Unit) = SpecBuilder.addBeforeEachHook(body)

    /**
     * Registers new "after" hook, which will be executed after each test
     */
    fun after(body: () -> Unit) = SpecBuilder.addAfterEachHook(body)

    /**
     * Registers new "before all" hook, which will be executed before first test in current suite
     */
    fun beforeAll(body: () -> Unit) = SpecBuilder.addBeforeAllHook(body)

    /**
     * Registers new "after all" hook, which will be executed last test in current suite
     */
    fun afterAll(body: () -> Unit) = SpecBuilder.addAfterAllHook(body)

    /**
     * Executes all "after" and "after all" (if it is last test) hooks
     */
    fun Internals.finalize(exceptionHandler: (AssertionError) -> Unit) {
        finalizers.forEach {
            it(exceptionHandler)
        }
    }


    /**
     * Executes all "before" and "before all" (if it is first test) hooks
     */
    fun Internals.initialize(exceptionHandler: (AssertionError) -> Unit) {
        initializers.forEach {
            it(exceptionHandler)
        }
    }

    /**
     * SPI for [[beforeAll]] for building custom DSL
     */
    fun SpecBuilder.addBeforeAllHook(body: () -> Unit) {
        modify {
            initializers += { withAssertionErrorHandling(it, body) }
        }
    }


    /**
     * SPI for [[afterAll]] for building custom DSL
     */
    fun SpecBuilder.addAfterAllHook(body: () -> Unit) {
        modify {
            finalizers += { withAssertionErrorHandling(it, body) }
        }
    }


    /**
     * SPI for [[after]] for building custom DSL
     */
    fun SpecBuilder.addAfterEachHook(body: () -> Unit) {
        modify {
            currentSuite.addInterceptor(Interceptor(after = executeEveryTime(body)))
        }
    }


    /**
     * SPI for [[before]] for building custom DSL
     */
    fun SpecBuilder.addBeforeEachHook(body: () -> Unit) {
        modify {
            currentSuite.addInterceptor(Interceptor(before = executeEveryTime(body)))
        }
    }

    /**
     * SPI with overloads for building custom DSL for specs suite definitions
     */
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

    /**
     * SPI with overloads for building custom DSL for specs tests definitions
     */
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

    private fun withAssertionErrorHandling(exceptionHandler: (AssertionError) -> Unit, action: () -> Unit) {
        try {
            action()
        } catch (e: AssertionError) {
            exceptionHandler(e)
        }
    }

    private fun closeResources(exceptionHandler: (AssertionError) -> Unit) {
        closeablesInReverseOrder.forEach {
            withAssertionErrorHandling(exceptionHandler, it::close)
        }
    }
}
