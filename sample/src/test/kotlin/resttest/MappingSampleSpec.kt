package resttest

import category.Positive
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import mockServer
import org.junit.runner.RunWith
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import pojo.Person
import run.smt.ktest.allure.*
import run.smt.ktest.resttest.expect
import run.smt.ktest.runner.junit4.KTestJUnitRunner
import run.smt.ktest.specs.WordSpec
import run.smt.ktest.util.resource.loadAsString

@RunWith(KTestJUnitRunner::class)
object MappingSampleSpec : WordSpec({
    beforeAll {
        mockServer.`when`(
            HttpRequest.request("/person/5").withMethod("GET")
        ).respond(HttpResponse.response(
            "mappingSample.json".loadAsString()
        ))
    }

    "My server" should {
        restTest(name = "return my person", metaInfo = {
            feature("RestTest usage")
            story("simple usage")
            title("Mapping: ${it.method}")

            category<Positive>()
        }) {
            debug = true

            url { backend / person / param("id") }

            GET(pathParam("id", 5))

            expect { response: Person ->
                assertThat(response, equalTo(
                    Person(5, "John Doe", -1)
                ))
            }
        }
    }
})
