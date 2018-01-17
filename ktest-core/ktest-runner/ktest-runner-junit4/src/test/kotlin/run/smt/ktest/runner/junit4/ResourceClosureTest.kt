package run.smt.ktest.runner.junit4

import org.junit.runner.RunWith
import run.smt.ktest.specs.FreeSpec
import java.io.Closeable

@RunWith(KTestJUnitRunner::class)
class ResourceClosureTest : FreeSpec() {

    private val resourceA = autoClose(Checker)
    private val resourceB = autoClose(Closeable2)
    private val resourceC = autoClose(Closeable1)

    init {
        "should close resources in reverse order" {
            // nothing to do here
        }
    }

    private object Closeable1 : Closeable {

        var closed = false

        override fun close() {
            closed = true
        }
    }

    private object Closeable2 : Closeable {

        var closed = false

        override fun close() {
            assert(Closeable1.closed)
            closed = true
        }
    }

    private object Checker : Closeable {
        override fun close() {
            assert(Closeable1.closed && Closeable2.closed)
        }

    }
}
