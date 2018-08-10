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
        val aTest = Case(Suite("", ExecutableCaseTest::class) {}, "", metaData = metaInfo {
            timeout(Duration.ofMillis(50))
        }) {
            Thread.sleep(30_000)
        }
        val executableCase = ExecutableCase(aTest, LifecycleNotifier(emptyList()))

        val testTook = measureTimeMillis {
            executableCase.execute()
        }

        Assert.assertTrue("Check that test took less than 200ms (100ms was timeout), test took: $testTook", testTook < 100)

    }
}
