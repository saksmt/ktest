package run.smt.ktest.jsonpath

import com.jayway.jsonpath.JsonPath
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.runner.RunWith
import run.smt.ktest.jsonpath.subtree.put
import run.smt.ktest.runner.junit4.KTestJUnitRunner
import run.smt.ktest.specs.WordSpec
import run.smt.ktest.util.text.stripMargin

@RunWith(KTestJUnitRunner::class)
object JsonSubtreeAdditionSpec : WordSpec({
    val json = """
        | {
        |   "outer" : {
        |       "inner": {
        |           "leaf": "leaf"
        |       }
        |   }
        | }
        """.stripMargin()

    "DocumentContext put" should {
        "add nodes by specified tree" {
            val jp = JsonPath.parse(json)
            val actual = jp.put {
                "outer" {
                    1 at "inner.newNode"

                    "inner" {
                        "world" at "hello"
                    }

                    "overridden" at "inner.leaf"
                }
            }.castTo { map<String, Any>() }

            assertThat(actual, equalTo<Map<String, Any>>(mapOf(
                "outer" to mapOf(
                    "inner" to mapOf(
                        "hello" to "world",
                        "leaf" to "overridden",
                        "newNode" to 1
                    )
                )
            )))
        }

        "support structural overrides (plain to object)" {
            val jp = JsonPath.parse(json)
            val actual = jp.put(force = true) {
                "truly leaf this time" at "outer.inner.leaf.trulyLeaf"
            }.castTo { map<String, Any>() }

            assertThat(actual, equalTo<Map<String, Any>>(mapOf(
                "outer" to mapOf(
                    "inner" to mapOf(
                        "leaf" to mapOf(
                            "trulyLeaf" to "truly leaf this time"
                        )
                    )
                )
            )))
        }

        "support structural overrides (object to plain)" {
            val jp = JsonPath.parse(json)
            val actual = jp.put {
                "hello" at "outer"
            }.castTo { map<String, Any>() }

            assertThat(actual, equalTo(mapOf<String, Any>("outer" to "hello")))
        }

        "support creation of sub-nodes" {
            val jp = JsonPath.parse(json)
            val actual = jp.put(force = true) {
                "otherBranch.inner" {
                    "world" at "hello"
                }
            }.castTo { map<String, Any>() }

            assertThat(actual, equalTo(mapOf<String, Any>(
                "outer" to mapOf("inner" to mapOf("leaf" to "leaf")),
                "otherBranch" to mapOf("inner" to mapOf("hello" to "world"))
            )))
        }
    }
})
