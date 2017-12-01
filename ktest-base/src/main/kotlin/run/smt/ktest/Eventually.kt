package run.smt.ktest

import java.time.Duration

fun <T>eventually(duration: Duration, f: () -> T): T {
  val end = System.nanoTime() + duration.nano
  var times = 0
  while (System.nanoTime() < end) {
    try {
      return f()
    } catch (e: Exception) {
      // ignore and proceed
    }
    times++
  }
  throw AssertionError("Test failed after ${duration.tun} ${duration.timeUnit}; attempted $times times")
}