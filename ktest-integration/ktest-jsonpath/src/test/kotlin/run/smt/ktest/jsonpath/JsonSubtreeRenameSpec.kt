package run.smt.ktest.jsonpath

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.throws
import org.junit.runner.RunWith
import run.smt.ktest.json.loadAsJson
import run.smt.ktest.jsonpath.subtree.rename
import run.smt.ktest.runner.junit4.KTestJUnitRunner
import run.smt.ktest.specs.WordSpec

@RunWith(KTestJUnitRunner::class)
object JsonSubtreeRenameSpec : WordSpec({
    "DocumentContext rename" should {
        "correctly rename fields" {
            val jp = "for-subtree.json".loadAsJsonPath()
            assertThat(jp.rename {
                "config.apps.app0.app2.core.datasources.app3" to "APP3"

                "config.apps" {
                    "app0.app2" {
                        "core" {
                            "idm" to "IDM"
                        }

                        "core.datasources.app2.url" to "URL"
                    }
                }
            }.castTo { map<String, Any>() }, equalTo("for-subtree-afterRename.json".loadAsJson { map<String, Any>() }))
        }

        "do nothing if no fields for rename specified" {
            val jp = "for-subtree.json".loadAsJsonPath()
            val untouched = "for-subtree.json".loadAsJsonPath()

            assertThat(
                jp.rename().castTo { map<String, Any>() },
                equalTo(untouched.castTo { map<String, Any>() })
            )
        }

        "fail on attempt to move nodes around" {
            assertThat({
                "for-subtree.json".loadAsJsonPath().rename {
                    "config" to "shms.config"
                }
            }, throws<IllegalArgumentException>())
        }

        "bug with rename to same name" {
            val jp = "for-subtree.json".loadAsJsonPath()
            val untouched = "for-subtree.json".loadAsJson<Map<String, Any>>()

            assertThat(jp.rename {
                "config.apps" to "apps"
            }.castTo { map<String, Any>() }, equalTo(untouched))
        }
    }
})
