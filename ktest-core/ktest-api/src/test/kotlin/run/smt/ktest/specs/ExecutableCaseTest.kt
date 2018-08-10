package run.smt.ktest.specs

import org.junit.Assert
import org.junit.Test
import run.smt.ktest.api.Case
import run.smt.ktest.api.Suite
import run.smt.ktest.api.internal.ExecutableCase
import run.smt.ktest.api.lifecycle.LifecycleNotifier
import run.smt.ktest.api.metaInfo
import java.time.Duration
import kotlin.system.measureTimeMillis

class ExecutableCaseTest {
    @Test
    fun `test should respect timeout`() {
        val timeout: Long = 50
        val aTest = Case(Suite("", ExecutableCaseTest::class) {}, "", metaData = metaInfo {
            timeout(Duration.ofMillis(timeout))
        }) {
            Thread.sleep(30_000)
        }
        val executableCase = ExecutableCase(aTest, LifecycleNotifier(emptyList()))

        val testTook = measureTimeMillis {
            executableCase.execute()
        }

        val allowedTimeout = 150
        Assert.assertTrue("Check that test took less than $allowedTimeout ms (timeout was: $timeout ms), test took: $testTook ms", testTook < allowedTimeout)

    }
}
