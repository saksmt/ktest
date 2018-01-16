package resttest

import com.fasterxml.jackson.databind.JsonNode
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import mockServer
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import run.smt.ktest.json.deserialize
import run.smt.ktest.json.matcher.hamkrest.JsonNodeMatchers
import run.smt.ktest.json.serialize
import run.smt.ktest.resttest.expect
import run.smt.ktest.specs.AllureSpec

class SimpleSpec : AllureSpec({
    beforeAll {
        mockServer.`when`(
            HttpRequest.request("/simple").withMethod("GET").withQueryStringParameter("a", "a")
        ).respond(HttpResponse.response(mapOf("state" to "updated").serialize().toString(charset = Charsets.UTF_8)).withStatusCode(200))

        mockServer.`when`(
            HttpRequest.request("/simple/postSpecific").withMethod("POST").withBody(mapOf("a" to "a").serialize())
        ).respond(HttpResponse.response().withStatusCode(200).withBody(mapOf("state" to "updated").serialize()))
    }

    feature("RestTest usage") {
        story("simple usage") {
            restTest(name = { "Simple request (with query params and body and matching): ${it.method}" }) {
                url { backend / simple }

                GET(queryParam("a" to "a"))
                POST(body("a" to "a")) { it / "postSpecific" }

                expect { response: JsonNode ->
                    with(JsonNodeMatchers) {
                        assertThat(response, isIdenticalTo("""
                            {
                                "state": "updated"
                            }
                        """.trimIndent() deserialize JsonNode::class))
                    }
                }
            }
        }
    }
})
