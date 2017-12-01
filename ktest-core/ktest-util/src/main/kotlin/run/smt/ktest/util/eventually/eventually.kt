package run.smt.ktest.util.eventually

import run.smt.ktest.util.duration.MillisBase
import run.smt.ktest.util.duration.inMillis
import run.smt.ktest.util.duration.inNanos
import java.time.Duration

data class Eventually internal constructor(
    private val awaitMillis: Int,
    private val delayMillis: Int? = null
) {
    infix fun <T> execute(f: (Int) -> T): T {
        var times = 0
        var lastException: Throwable? = null
        if (delayMillis ?: -1 > awaitMillis) {
            throw IllegalArgumentException("Setting delay more than total await time is non-sense!")
        }
        val end = System.currentTimeMillis() + awaitMillis.toLong()
        while (System.currentTimeMillis() < end) {
            lastException = try {
                return f(times)
            } catch (e: Exception) {
                e
            } catch (e: AssertionError) {
                e
            }
            times++
            delayMillis?.toLong()?.let(Thread::sleep)
        }
        throw AssertionError(
            "Test failed after ${inNanos { awaitMillis.millis().show() }}; attempted $times times. " +
                "Last attempt failed with: $lastException"
        )
    }

    infix fun withDelay(delayMillis: Int) = copy(delayMillis = delayMillis)
    infix fun withDelay(time: MillisBase.() -> Int) = withDelay(inMillis(time))
    infix fun withDelay(duration: Duration) = withDelay(duration.toMillis().toInt())
}

fun <T> eventually(duration: Duration, f: () -> T): T = within { duration.toMillis().toInt().millis() } execute { f() }

fun within(time: MillisBase.() -> Int) = Eventually(inMillis(time))
