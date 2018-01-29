package rest

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import io.restassured.response.Response
import mockServer
import org.junit.runner.RunWith
import org.mockserver.model.Header
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import run.smt.ktest.json.deserialize
import run.smt.ktest.json.type
import run.smt.ktest.rest.rest
import run.smt.ktest.runner.junit4.KTestJUnitRunner
import run.smt.ktest.specs.AllureSpec
import java.io.InputStream

data class SamplePojo(val hello: String)

@RunWith(KTestJUnitRunner::class)
object SimpleRestRequestsSpec : AllureSpec({
    feature("REST usage") {
        story("simple requests") {
            case("GET with typeDSL") {
                val response = rest {
                    "/rest/simple".GET(queryParam("simpleRequest", "simpleRequest")) { map<String, String>() }
                }

                assertThat(response, equalTo(mapOf("hello" to "world")))
            }

            case("PUT with reified deserialization") {
                val response: SamplePojo = rest {
                    "/rest/simple".PUT(
                        header("x-my-company-header", "ktest")
                    )
                }

                assertThat(response, equalTo(SamplePojo("world")))
            }

            case("POST with explicit deserialization") {
                val myData = SamplePojo("world")

                val response = rest {

                    "/rest/{type}".POST(
                        SamplePojo::class,
                        pathParam("type", "simple"),
                        body(myData)
                    )
                }

                assertThat(response, equalTo(myData))
            }

            case("PATCH with deserialization as string") {
                val response = rest {
                    "/rest/simple".PATCH<String>()
                }

                assertThat(response deserialize { map<String, String>() }, equalTo(mapOf("hello" to "world")))
            }

            case("DELETE with deserialization as InputStream") {
                val response = rest {
                    "/rest/simple".DELETE<InputStream>()
                }

                assertThat(response deserialize { map<String, String>() }, equalTo(mapOf("hello" to "world")))
            }

            case("GET with deserialization as Pair(statusCode, data) and failure status code") {

                val (statusCode, response) = rest {
                    // here is how you can nicely mix named parameters, varargs and lambda at the same time!
                    "/rest/simple".GET(
                        queryParam("fail", "true"),
                        ignoreStatusCode = true
                    ) { pair(simple<Int>(), map<String, String>()) }
                }

                assertThat(statusCode, equalTo(418))
                assertThat(response, equalTo(mapOf("hello" to "world")))
            }
        }
    }

    beforeAll {
        val contentType = Header.header("Content-Type", "application/json; charset=utf-8")
        val body = """
                    {
                        "hello": "world"
                    }
                """.trimIndent()

        mockServer.`when`(
            HttpRequest.request("/rest/simple")
                .withMethod("GET")
                .withQueryStringParameter("simpleRequest", "simpleRequest")
        ).respond(
            HttpResponse.response(body).withHeader(contentType)
        )

        mockServer.`when`(
            HttpRequest.request("/rest/simple")
                .withMethod("PUT")
                .withHeader("x-my-company-header", "ktest")
        ).respond(
            HttpResponse.response(body).withHeader(contentType)
        )

        mockServer.`when`(
            HttpRequest.request("/rest/simple")
                .withMethod("POST")
        ).callback { HttpResponse.response(it.bodyAsString).withHeader(contentType) }

        mockServer.`when`(
            HttpRequest.request("/rest/simple").withMethod("PATCH")
        ).respond(HttpResponse.response(body).withHeader(contentType))

        mockServer.`when`(
            HttpRequest.request("/rest/simple").withMethod("DELETE")
        ).respond(HttpResponse.response(body).withHeader(contentType))

        mockServer.`when`(
            HttpRequest.request("/rest/simple").withMethod("GET").withQueryStringParameter("fail", "true")
        ).respond(HttpResponse.response(body).withHeader(contentType).withStatusCode(418))
    }
})
