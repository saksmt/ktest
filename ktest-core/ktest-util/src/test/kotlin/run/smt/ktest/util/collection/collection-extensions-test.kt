package run.smt.ktest.util.collection

import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.should.shouldMatch
import org.junit.Test

class CollectionExtensionsTest {

    @Test
    fun `test Sequence permutations`() {
        sequenceOf(1, 2, 3).permutations().map { it.toList() }.sortedBy { it.toString() }.toList() shouldMatch equalTo(listOf(
            listOf(1, 2, 3),
            listOf(2, 1, 3),
            listOf(1, 3, 2),
            listOf(3, 1, 2),
            listOf(3, 2, 1),
            listOf(2, 3, 1)
        ).sortedBy { it.toString() })
    }

    @Test
    fun `test Sequence crossProduct`() {
        val actual: Sequence<Pair<Int, Int>> = sequenceOf(1, 2, 3).crossProduct(sequenceOf(4, 5, 6))
        actual.sortedBy { it.toString() }.toList() shouldMatch equalTo(listOf(
            1 to 4,
            1 to 5,
            1 to 6,
            2 to 4,
            2 to 5,
            2 to 6,
            3 to 4,
            3 to 5,
            3 to 6
        ).sortedBy { it.toString() })
    }

}
