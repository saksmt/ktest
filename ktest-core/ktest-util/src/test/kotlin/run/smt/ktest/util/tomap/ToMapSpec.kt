package run.smt.ktest.util.tomap

import io.kotlintest.matchers.shouldEqual
import io.kotlintest.specs.ShouldSpec

data class MyNestedPojo(val nestedF1: String, val nestedF2: Map<String, String>)

data class MyPojo(
    val f1: Int,
    val f2: List<String>,
    val f3: MyNestedPojo
)

class ToMapSpec : ShouldSpec({
    val testPojo = MyPojo(1, listOf("a", "b"), MyNestedPojo("hello", mapOf("hello" to "world", "test" to "test")))
    "toMap" {
        should("convert POJO to map") {
            testPojo.toMap(deep = false) shouldEqual mapOf(
                "f1" to 1,
                "f2" to listOf("a", "b"),
                "f3" to MyNestedPojo("hello", mapOf("hello" to "world", "test" to "test"))
            )
        }

        should("convert POJO and all it's fields to map if deep was set") {
            testPojo.toMap(deep = true) shouldEqual mapOf(
                "f1" to 1,
                "f2" to listOf("a", "b"),
                "f3" to mapOf(
                    "nestedF1" to "hello",
                    "nestedF2" to mapOf("hello" to "world", "test" to "test")
                )
            )
        }
    }
})
