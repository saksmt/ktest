package run.smt.ktest.util.reflection

import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.should.shouldMatch
import org.junit.Test

class ReflectionUtilsTest {
    private val isTrue = equalTo(true)
    private val isFalse = equalTo(false)

    @Test
    fun `test canBeAssigned`() {
        Long::class canBeAssignedTo Number::class shouldMatch isTrue
        Number::class canBeAssignedTo Long::class shouldMatch isFalse
        Map::class canBeAssignedTo Map::class shouldMatch isTrue
        Int::class canBeAssignedTo Long::class shouldMatch isFalse
        String::class canBeAssignedTo Long::class shouldMatch isFalse
    }
}
