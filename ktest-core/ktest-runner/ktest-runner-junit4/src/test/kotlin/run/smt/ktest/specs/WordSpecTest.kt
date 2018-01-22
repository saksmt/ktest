package run.smt.ktest.specs

import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.should.shouldMatch
import org.junit.runner.RunWith
import run.smt.ktest.ListStack
import run.smt.ktest.runner.junit4.KTestJUnitRunner

@RunWith(KTestJUnitRunner::class)
class WordSpecTest : WordSpec({
    "ListStack.pop" should {
        "remove the last element from stack" {
            val stack = ListStack<String>()
            stack.push("hello")
            stack.push("world")
            stack.size() shouldMatch equalTo(2)
            stack.pop() shouldMatch equalTo("world")
            stack.size() shouldMatch equalTo(1)
        }
        "remove the last element from stack again" {
            val stack = ListStack<String>()
            stack.push("hello")
            stack.push("world")
            stack.size() shouldMatch equalTo(2)
            stack.pop() shouldMatch equalTo("world")
            stack.size() shouldMatch equalTo(1)
        }
    }
    "ListStack.peek" should {
        "should leave the stack unmodified" {
            val stack = ListStack<String>()
            stack.push("hello")
            stack.push("world")
            stack.size() shouldMatch equalTo(2)
            stack.peek() shouldMatch equalTo("world")
            stack.size() shouldMatch equalTo(2)
        }
    }
})
