package run.smt.ktest.jsonpath

import com.jayway.jsonpath.DocumentContext
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import run.smt.ktest.specs.WordSpec

class JsonPathExtractionSpec : WordSpec({
    val jsonPath = "find.json".loadAsJsonPath()

    "DocumentContext[]" should {
        "return complex value by path" {
            assertThat(
                jsonPath["$.a.a1"].castTo { map(String::class, Any::class) },
                equalTo(mapOf(
                    "c2" to 2,
                    "b2" to "b2"
                ))
            )
        }

        "return primitive value by path" {
            assertThat(jsonPath["$.c"] castTo Int::class, equalTo(0))
        }
    }

    "SelectLikeDSL" should {
        "return complex value by path" {
            val actual = jsonPath select "$.a.a1[?]" where { "b2" isOfType String::class }

            assertThat(
                actual.castTo { list(map(String::class, Any::class)) },
                equalTo(listOf(mapOf(
                    "c2" to 2,
                    "b2" to "b2"
                )))
            )
        }

        "return primitive value by path" {
            val actual = select("$.b") from jsonPath where { "$".nonEmpty() }

            assertThat(actual castTo String::class, equalTo("b"))
        }
    }
})
