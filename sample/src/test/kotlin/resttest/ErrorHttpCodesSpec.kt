package resttest

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.isEmptyString
import mockServer
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import run.smt.ktest.resttest.expect
import run.smt.ktest.specs.AllureSpec
import run.smt.ktest.specs.FreeSpec

class ErrorHttpCodesSpec : FreeSpec({
    beforeAll {
        mockServer.`when`(
            HttpRequest.request("/errorHttpCodes").withMethod("PUT")
        ).respond(HttpResponse.response().withStatusCode(418))

        mockServer.`when`(
            HttpRequest.request("/errorHttpCodes").withMethod("DELETE")
        ).respond(HttpResponse.response().withStatusCode(418))
    }

    "RestTest usage" - {
        "simple usage" - {
            restTest(name = { "HTTP error codes: ${it.method}" }, metaInfo = {
                feature("RestTest usage")
                story("simple usage")
                title("HTTP error codes: ${it.method}")
            }) {
                url { backend / errorHttpCodes }

                PUT()
                DELETE()

                expect { statusCode: Int, response: String ->
                    assertThat(statusCode, equalTo(418))

                    assertThat(response, isEmptyString)
                }
            }
        }
    }
})
