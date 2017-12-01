package run.smt.ktest

import org.junit.Assert.assertTrue
import org.junit.runner.Description
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunListener
import org.junit.runner.notification.RunNotifier
import run.smt.ktest.internal.KTestJUnitRunner
import run.smt.ktest.specs.FreeSpec
import run.smt.ktest.specs.SimpleSpec
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class KTestJunitRunnerTest : SimpleSpec() {
    init {
        test("should handle AssertionError") {
            System.setProperty("internal", "true")
            val runner = KTestJUnitRunner(ThrowsAssertionError::class.java as Class<BaseSpec>)
            val n = RunNotifier()
            val latch = CountDownLatch(1)
            n.addListener(object : RunListener() {
                override fun testFailure(failure: Failure?) {
                    latch.countDown()
                }
            })
            runner.run(n)
            assertTrue(latch.await(5, TimeUnit.SECONDS))
            System.setProperty("internal", "false")
        }

        test("should handle ignored tests") {
            val runner = KTestJUnitRunner(HasIgnoredTest::class.java as Class<BaseSpec>)
            val n = RunNotifier()
            val latch = CountDownLatch(1)
            n.addListener(object : RunListener() {
                override fun testIgnored(description: Description?) {
                    latch.countDown()
                }
            })
            runner.run(n)
            assertTrue(latch.await(5, TimeUnit.SECONDS))
        }

        test("should report exceptions as an error") {
            System.setProperty("internal", "true")
            val runner = KTestJUnitRunner(ThrowsRuntimeException::class.java as Class<BaseSpec>)
            val n = RunNotifier()
            val latch = CountDownLatch(1)
            n.addListener(object : RunListener() {
                override fun testFailure(failure: Failure?) {
                    latch.countDown()
                }
            })
            runner.run(n)
            assertTrue(latch.await(5, TimeUnit.SECONDS))
            System.setProperty("internal", "false")
        }
    }
}

class ThrowsAssertionError : SimpleSpec() {
    init {
        test("throw throwable") {
            if (System.getProperty("internal") == "true")
                throw AssertionError("hello")
        }
    }
}

class ThrowsRuntimeException : SimpleSpec() {
    init {
        test("throw throwable") {
            if (System.getProperty("internal") == "true")
                throw RuntimeException("hello")
        }
    }
}

class HasIgnoredTest : FreeSpec() {
    init {
        "ignored test" {
        }.config(enabled = false)
    }
}
