package resttest

import category.Positive
import category.Smoke
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import mockServer
import org.junit.runner.RunWith
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import run.smt.ktest.specs.SimpleSpec
import run.smt.ktest.resttest.expect
import run.smt.ktest.allure.*
import run.smt.ktest.runner.junit4.KTestJUnitRunner

@RunWith(KTestJUnitRunner::class)
object PingSpec : SimpleSpec({
    beforeAll {
        mockServer.`when`(
            HttpRequest.request("/ping").withMethod("GET")
        ).respond(HttpResponse.response("Alive").withStatusCode(200))

        mockServer.`when`(
            HttpRequest.request("/ping").withMethod("POST")
        ).respond(HttpResponse.response("Alive").withStatusCode(200))
    }

    suite("RestTest usage") {
        suite("simple usage") {
            restTest(name = { "Ping (simple matching): ${it.method}" }, metaInfo = {
                feature("RestTest usage")
                story("simple usage")
                title("Ping (simple matching): ${it.method}")

                category<Positive>()
                category<Smoke>()
            }) {
                url { backend / ping }

                GET()
                POST()

                expect { response: String ->
                    assertThat(response, equalTo("Alive"))
                }
            }
        }
    }
})
