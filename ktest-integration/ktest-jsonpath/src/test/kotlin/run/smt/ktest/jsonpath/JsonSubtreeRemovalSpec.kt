package run.smt.ktest.jsonpath

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import run.smt.ktest.jsonpath.subtree.remove
import run.smt.ktest.specs.WordSpec

class JsonSubtreeRemovalSpec : WordSpec({
    "DocumentContext remove" should {
        "correctly drop specified nodes" {
            val jp = "for-subtree.json".loadAsJsonPath()

            assertThat(jp.remove {
                + "config" {
                    + "apps.app0.app2" {
                        + "core.datasources"
                        + "core.idm" {
                            + "predicates"
                        }
                    }

                    + "will"
                }

                + "will.be"
            }.castTo { map<String, Any>() }, equalTo(mapOf<String, Any>(
                "config" to mapOf(
                    "apps" to mapOf(
                        "app0" to mapOf(
                            "app2" to mapOf(
                                "core" to mapOf(
                                    "cache" to "cache",
                                    "idm" to mapOf(
                                        "operations" to "operations"
                                    )
                                ),
                                "beanValidationMapper" to "beanValidationMapper",
                                "goalSearch" to "goalSearch"
                            )
                        )
                    )
                ),
                "will" to emptyMap<String, Any>()
            )))
        }
    }
})
