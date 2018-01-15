package run.smt.ktest.jsonpath

import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.PathNotFoundException
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.throws
import run.smt.ktest.jsonpath.criteria.filter
import run.smt.ktest.jsonpath.subtree.createSubtree
import run.smt.ktest.jsonpath.subtree.extractSubtree
import run.smt.ktest.specs.WordSpec
import run.smt.ktest.util.text.stripMargin

class JsonSubtreeExtractionSpec : WordSpec({
    val subtreeWithMissing = createSubtree {
        + "config.apps.app0.app2" {
            + "core" {
                + "datasources" {
                    + "app2"
                    + "app1"
                }

                + "cache"

                + filter { "@.predicates".exists() }
            }

            + "core.datasources.app3"
            + "core.datasources.app2"
        }

        + filter {
            ("@" isOfType String::class).and("@").empty()
        }
    }

    val subtreeWithoutMissing = createSubtree {
        + "config.apps.app0.app2.core.datasources.app3"
    }

    val expectedForNoMissing = mapOf<String, Any>(
        "config" to mapOf(
            "apps" to mapOf(
                "app0" to mapOf(
                    "app2" to mapOf(
                        "core" to mapOf(
                            "datasources" to mapOf(
                                "app3" to mapOf("url" to "app3_url")
                            )
                        )
                    )
                )
            )
        )
    )

    "JsonSubtreeDSL" should {
        "generate correct path specs" {
            val paths = subtreeWithMissing.mapNotNull { it.path }

            assertThat(paths.sorted(), equalTo(listOf(
                "$.config.apps.app0.app2.core.datasources.app2",
                "$.config.apps.app0.app2.core.datasources.app1",
                "$.config.apps.app0.app2.core.cache",
                "$.config.apps.app0.app2.core..*[?]",
                "$.config.apps.app0.app2.core.datasources.app3",
                "$..*[?]"
            ).sorted()))
        }
    }

    "subtree with ignoreMissing" should {
        "extract requested subtree from JsonPath DocumentContext" {
            val source = "for-subtree.json".loadAsJsonPath()

            val actual = extractSubtree(source, subtreeWithoutMissing, ignoreMissing = true)
                .castTo<Map<String, Any>> { map<String, Any>() }

            assertThat(actual, equalTo(expectedForNoMissing))
        }

        "extract requested subtree from JsonPath DocumentContext even if there missing nodes" {
            val source = "for-subtree.json".loadAsJsonPath()
            val actual = extractSubtree(source, subtreeWithMissing, ignoreMissing = true)
                .castTo<Map<String, Any>> { map<String, Any>() }
            assertThat(actual, equalTo(mapOf<String, Any>(
                "config" to mapOf(
                    "apps" to mapOf(
                        "app0" to mapOf(
                            "app2" to mapOf(
                                "core" to mapOf(
                                    "datasources" to mapOf(
                                        "app3" to mapOf("url" to "app3_url"),
                                        "app2" to mapOf("url" to "app2_url"),
                                        "app1" to mapOf("url" to "app1_url")
                                    ),
                                    "idm" to mapOf(
                                        "operations" to "operations",
                                        "predicates" to "predicates"
                                    ),
                                    "cache" to "cache"
                                )
                            )
                        )
                    )
                )
            )))
        }
    }

    "subtree without ignoreMissing" should {
        "extract requested subtree from JsonPath DocumentContext" {
            val source = "for-subtree.json".loadAsJsonPath()

            val actual = extractSubtree(source, subtreeWithoutMissing)
                .castTo<Map<String, Any>> { map<String, Any>() }

            assertThat(actual, equalTo(expectedForNoMissing))
        }

        "throw exception when requested to subtree which can not be fully filled" {
            val source = "for-subtree.json".loadAsJsonPath()

            assertThat({
                extractSubtree(source, subtreeWithMissing)
            }, throws<PathNotFoundException>())
        }

        "support dsl-ish calls" {
            val source = "for-subtree.json".loadAsJsonPath()

            val actual = extractSubtree(source) {
                + "config.apps.app0.app2.core.datasources.app3"
            }.castTo<Map<String, Any>> { map<String, Any>() }

            assertThat(actual, equalTo(expectedForNoMissing))
        }

        "respect filters" {
            val sourceJson = """
                | {
                |   "a": {
                |       "b": {
                |          "b": "hello",
                |          "c": "don't need this"
                |       }
                |   },
                |   "a1": { "b": "but need this" }
                | }
                """.stripMargin()
            val source = JsonPath.parse(sourceJson)

            val actual = extractSubtree(source) {
                + "a.b.b"

                + filter {
                    "@.b" eq "but need this"
                }
            }.castTo<Map<String, Any>> { map<String, Any>() }

            assertThat(actual, equalTo(mapOf<String, Any>(
                "a" to mapOf(
                    "b" to mapOf(
                        "b" to "hello"
                    )
                ),
                "a1" to mapOf("b" to "but need this")
            )))
        }

        "return emptiness on empty subtree request" {
            val actual: Map<String, Any> = extractSubtree("for-subtree.json".loadAsJsonPath()) {} castTo { map<String, Any>() }
            assertThat(actual, equalTo(emptyMap()))
        }
    }
})
