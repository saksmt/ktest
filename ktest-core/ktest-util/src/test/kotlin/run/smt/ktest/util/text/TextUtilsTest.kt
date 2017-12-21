package run.smt.ktest.util.text

import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.should.shouldMatch
import org.junit.Test

class TextUtilsTest {
    @Test
    fun `test stripMargin`() {
        """
            | Hello
awful       | world
            """.stripMargin() shouldMatch equalTo("\n Hello\nawful       | world\n            ")
    }
}
