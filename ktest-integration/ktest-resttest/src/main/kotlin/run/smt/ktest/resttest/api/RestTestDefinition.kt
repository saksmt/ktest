package run.smt.ktest.resttest.api

import com.fasterxml.jackson.databind.JavaType
import run.smt.ktest.api.MetaInfoBuilder
import run.smt.ktest.json.TypeDSL
import run.smt.ktest.json.type
import run.smt.ktest.rest.api.RequestElement
import run.smt.ktest.rest.api.RequestElementBuilder
import run.smt.ktest.rest.api.RestContext
import run.smt.ktest.rest.url.UrlDsl
import run.smt.ktest.rest.url.UrlProvider
import kotlin.reflect.KClass

typealias RestMetaInfoBuilder = MetaInfoBuilder.(RequestData) -> Unit
typealias RestTestDSL<U> = RestTestDefinition<U>.() -> Unit
typealias Expectation<T> = (T) -> Unit
typealias StatusCodeAwareExpectation<T> = (Int, T) -> Unit

// marker object for hiding some properties from completion
object RestTestInternals
/**
 * Receiver for REST Test DSL
 */
interface RestTestDefinition<out U : UrlProvider> : RequestElementBuilder {
    val RestTestInternals.urlDsl: UrlDsl<U>

    var url: String

    var metaInfo: RestMetaInfoBuilder?
    var debug: Boolean

    var context: RestContext?

    fun metaInfo(metaInfoBuilder: RestMetaInfoBuilder) {
        metaInfo = metaInfoBuilder
    }

    fun url(urlBuilder: U.() -> String) {
        url = RestTestInternals.urlDsl(urlBuilder)
    }

    fun using(restDsl: RestContext) {
        context = restDsl
    }

    fun GET(vararg parameters: RequestElement)
    fun POST(vararg parameters: RequestElement)
    fun PUT(vararg parameters: RequestElement)
    fun HEAD(vararg parameters: RequestElement)
    fun OPTIONS(vararg parameters: RequestElement)
    fun PATCH(vararg parameters: RequestElement)
    fun DELETE(vararg parameters: RequestElement)

    fun GET(vararg parameters: RequestElement, urlBuilder: U.(String) -> String) = GET(RestTestInternals.urlDsl { urlBuilder(url) }, *parameters)
    fun POST(vararg parameters: RequestElement, urlBuilder: U.(String) -> String) = POST(RestTestInternals.urlDsl { urlBuilder(url) }, *parameters)
    fun PUT(vararg parameters: RequestElement, urlBuilder: U.(String) -> String) = PUT(RestTestInternals.urlDsl { urlBuilder(url) }, *parameters)
    fun HEAD(vararg parameters: RequestElement, urlBuilder: U.(String) -> String) = HEAD(RestTestInternals.urlDsl { urlBuilder(url) }, *parameters)
    fun OPTIONS(vararg parameters: RequestElement, urlBuilder: U.(String) -> String) = OPTIONS(RestTestInternals.urlDsl { urlBuilder(url) }, *parameters)
    fun PATCH(vararg parameters: RequestElement, urlBuilder: U.(String) -> String) = PATCH(RestTestInternals.urlDsl { urlBuilder(url) }, *parameters)
    fun DELETE(vararg parameters: RequestElement, urlBuilder: U.(String) -> String) = DELETE(RestTestInternals.urlDsl { urlBuilder(url) }, *parameters)

    fun GET(url: String, vararg parameters: RequestElement)
    fun POST(url: String, vararg parameters: RequestElement)
    fun PUT(url: String, vararg parameters: RequestElement)
    fun HEAD(url: String, vararg parameters: RequestElement)
    fun OPTIONS(url: String, vararg parameters: RequestElement)
    fun PATCH(url: String, vararg parameters: RequestElement)
    fun DELETE(url: String, vararg parameters: RequestElement)

    fun <T : Any> expect(resultType: KClass<T>, expectation: Expectation<T>) = expect(resultType) { code, response ->
        if (code !in 200..399) {
            throw AssertionError("Status code is not 2xx")
        }
        expectation(response)
    }

    fun <T : Any> expect(type: JavaType, expectation: Expectation<T>) {
        expect<T>(type) { code, response ->
            if (code !in 200..399) {
                throw AssertionError("Status code is not 2xx")
            }
            expectation(response)
        }
    }

    fun <T : Any> expect(type: TypeDSL, expectation: Expectation<T>) = expect(type(type), expectation)

    fun <T : Any> expect(resultType: KClass<T>, expectation: StatusCodeAwareExpectation<T>)
    fun <T : Any> expect(type: JavaType, expectation: StatusCodeAwareExpectation<T>)
    fun <T : Any> expect(type: TypeDSL, expectation: StatusCodeAwareExpectation<T>) = expect(type(type), expectation)
}

