package rest

import mockServer
import org.hamcrest.CoreMatchers.equalTo
import org.junit.runner.RunWith
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import run.smt.ktest.rest.rest
import run.smt.ktest.runner.junit4.KTestJUnitRunner
import run.smt.ktest.specs.AllureSpec

@RunWith(KTestJUnitRunner::class)
object ComplexRestRequestsSpec : AllureSpec({
    val requestBody = """
                    {
                        "hello": "world"
                    }
                """.trimIndent()
    beforeAll {
        mockServer.`when`(
            HttpRequest.request("/rest/complex")
                .withMethod("POST")
                .withBody(requestBody)
        ).respond(
            HttpResponse.response("""
                {
                    "response": {
                        "status": "ok"
                    }
                }
            """.trimIndent()).withStatusCode(235).withHeader("Content-Type", "application/json;charset=utf-8")
        )
    }

    feature("REST usage") {
        story("complex usage") {
            case("complex request") {
                rest {
                    request(
                        body(requestBody)
                    ).`when`().post(
                        "/rest/complex"
                    ).then().assertThat()
                        .statusCode(235)
                        .and().body("response.status", equalTo("ok"))
                        .and().header("Content-Type", equalTo("application/json;charset=utf-8"))
                }
            }
        }
    }
})
