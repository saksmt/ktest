package run.smt.ktest.specs

import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.should.shouldMatch
import run.smt.ktest.ListStack

class SimpleSpecTest : SimpleSpec({
    test("ListStack.pop should remove the last element from stack") {
        val stack = ListStack<String>()
        stack.push("hello")
        stack.push("world")
        stack.size() shouldMatch equalTo(2)
        stack.pop() shouldMatch equalTo("world")
        stack.size() shouldMatch equalTo(1)
    }

    test("ListStack.peek should leave the stack unmodified") {
        val stack = ListStack<String>()
        stack.push("hello")
        stack.push("world")
        stack.size() shouldMatch equalTo(2)
        stack.peek() shouldMatch equalTo("world")
        stack.size() shouldMatch equalTo(2)
    }

    test("FunSpec should support config syntax") {
    }.config(invocations = 5)
})
