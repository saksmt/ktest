package run.smt.ktest.specs

import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.should.shouldMatch
import run.smt.ktest.ListStack

class FeatureSpecTest : FeatureSpec({

    feature("ListStack can have elements removed") {
        scenario("pop is invoked") {
            val stack = ListStack<String>()
            stack.push("hello")
            stack.push("world")
            stack.size() shouldMatch equalTo(2)
            stack.pop() shouldMatch equalTo("world")
            stack.size() shouldMatch equalTo(1)
        }
    }

    feature("featurespec") {
        scenario("support config syntax") {
        }.config(invocations = 5)
    }
})
