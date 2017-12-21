package run.smt.ktest.util.resource

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.isA
import com.natpryce.hamkrest.should.shouldMatch
import com.natpryce.hamkrest.throws
import org.junit.Test
import java.nio.file.NoSuchFileException

class ResourceTest {
    private val isTrue = equalTo(true)
    private val isFalse = equalTo(false)

    @Test
    fun `test loadAsString`() {
        "test-resource".loadAsString() shouldMatch equalTo("hello\n")
        assertThat({ "!!!NON EXISTENT!!!".loadAsString() }, throws(isA<NoSuchFileException>()))
    }

    @Test
    fun `test resourceExists`() {
        "test-resource".resourceExists() shouldMatch isTrue
        "!!!NON EXISTENT!!!".resourceExists() shouldMatch isFalse
    }
}
