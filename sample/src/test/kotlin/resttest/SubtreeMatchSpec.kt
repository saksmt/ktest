package resttest

import category.Positive
import com.fasterxml.jackson.databind.JsonNode
import com.natpryce.hamkrest.assertion.assertThat
import mockServer
import org.junit.runner.RunWith
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import run.smt.ktest.allure.*
import run.smt.ktest.json.loadAsJsonTree
import run.smt.ktest.json.matcher.hamkrest.JsonNodeMatchers
import run.smt.ktest.resttest.expect
import run.smt.ktest.runner.junit4.KTestJUnitRunner
import run.smt.ktest.specs.BehaviorSpec
import run.smt.ktest.util.resource.loadAsString

@RunWith(KTestJUnitRunner::class)
object SubtreeMatchSpec : BehaviorSpec({
    beforeAll {
        mockServer.`when`(
            HttpRequest.request("/complexJson").withMethod("PATCH")
        ).respond(HttpResponse.response("complexResponse.json".loadAsString()))
    }

    given("my service") {
        `when`("requesting for complex json") {
            val expected = "complexResponse-expectation.json".loadAsJsonTree()

            restTest(name = "it should match expected by subtree", metaInfo = {
                feature("RestTest usage")
                story("simple usage")
                title("Matching by subtree: ${it.method}")

                category<Positive>()
            }) {
                url { backend / complexJson }

                PATCH()

                expect { response: JsonNode ->
                    with(JsonNodeMatchers) {
                        assertThat(response, matches(expected) bySubtree {
                            "rootObject" {
                                "level1Object" {
                                    "level2Object" {
                                        + "level3Object.level4Value3"
                                        + "level3Value1"
                                        + "level3Value3"
                                    }
                                    + "level2Array"
                                    + "level2Value1"
                                    + "level2Value2"
                                }
                            }
                            + "rootValue1"
                            + "rootValue3"
                        })
                    }
                }
            }
        }
    }
})
