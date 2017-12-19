package run.smt.ktest.resttest.impl

import com.fasterxml.jackson.databind.JavaType
import io.restassured.response.Response
import run.smt.ktest.allure.AllureMetaInfoDSL
import run.smt.ktest.allure.metaInfo
import run.smt.ktest.rest.api.Deserialization
import run.smt.ktest.rest.api.RequestBuilder
import run.smt.ktest.rest.api.RequestElement
import run.smt.ktest.rest.api.RestContext
import run.smt.ktest.rest.url.UrlDsl
import run.smt.ktest.rest.url.UrlProvider
import run.smt.ktest.resttest.api.*
import run.smt.ktest.util.functional.Either.Either
import run.smt.ktest.util.functional.Either.left
import run.smt.ktest.util.functional.Either.right
import kotlin.reflect.KClass

private typealias UntypedExpectation = (Int, Any) -> Unit
private typealias TypedExpectation = Pair<Either<KClass<*>, JavaType>, UntypedExpectation>
private typealias RequestSpec = RequestBuilder.() -> Response
private typealias NamedRequestSpec = Pair<RequestData, RequestSpec>

internal open class RestTestDefinitionImpl<U : UrlProvider>(protected val restParams: RestTestParams<U>) : RestTestDefinition<U>, Deserialization {
    override val RestTestInternals.urlDsl: UrlDsl<U> get() = restParams.urlDsl ?: throw IllegalArgumentException("You must specify URL DSL!")

    private val expectations: MutableList<TypedExpectation> = mutableListOf()
    private val requests: MutableList<NamedRequestSpec> = mutableListOf()

    override lateinit var url: String
    override var metaInfo: RestMetaInfoBuilder? = null
    override var debug: Boolean = false
    override var context: RestContext? = null

    /**
     * Push new request to list of requests to be executed
     */
    private fun delayed(data: RequestData, request: RequestSpec) = Pair(data, request)

    private fun save(data: RequestData, request: RequestSpec) {
        requests.add(delayed(data, request))
    }

    override fun <T : Any> expect(resultType: KClass<T>, expectation: StatusCodeAwareExpectation<T>) {
        expectations.add(left<KClass<*>, JavaType>(resultType) to @Suppress("UNCHECKED_CAST") (expectation as UntypedExpectation))
    }

    override fun <T : Any> expect(type: JavaType, expectation: StatusCodeAwareExpectation<T>) {
        expectations.add(right<KClass<*>, JavaType>(type) to @Suppress("UNCHECKED_CAST") (expectation as UntypedExpectation))
    }

    private operator fun RestMetaInfoBuilder.invoke(data: RequestData): AllureMetaInfoDSL = { (metaInfo ?: this@invoke)(data) }

    internal fun execute(nameGen: (RequestData) -> String, metaInfo: RestMetaInfoBuilder, withSkel: (List<Annotation>, String, () -> Unit) -> Unit) {
        val actualRestContext = context ?: restParams.restDsl
        requests.forEach { (requestData, buildRequest) ->
            withSkel(
                metaInfo(metaInfo(requestData)),
                nameGen(requestData)
            ) {
                val response = actualRestContext {
                    debug = this@RestTestDefinitionImpl.debug
                    buildRequest()
                }

                val statusCode = response.statusCode

                expectations.forEach { (resultType, runExpectation) ->
                    runExpectation(statusCode, resultType.unify({ response.`as`(it) }, { response.`as`(it) }))
                }
            }
        }
    }


    override fun GET(vararg parameters: RequestElement) = save(RequestData(url, "GET")) { url.GET(*parameters, ignoreStatusCode = true) }
    override fun POST(vararg parameters: RequestElement) = save(RequestData(url, "POST")) { url.POST(*parameters, ignoreStatusCode = true) }
    override fun PUT(vararg parameters: RequestElement) = save(RequestData(url, "PUT")) { url.PUT(*parameters, ignoreStatusCode = true) }
    override fun HEAD(vararg parameters: RequestElement) = save(RequestData(url, "HEAD")) { url.HEAD(*parameters, ignoreStatusCode = true) }
    override fun OPTIONS(vararg parameters: RequestElement) = save(RequestData(url, "OPTIONS")) { url.OPTIONS(*parameters, ignoreStatusCode = true) }
    override fun PATCH(vararg parameters: RequestElement) = save(RequestData(url, "PATCH")) { url.PATCH(*parameters, ignoreStatusCode = true) }
    override fun DELETE(vararg parameters: RequestElement) = save(RequestData(url, "DELETE")) { url.DELETE(*parameters, ignoreStatusCode = true) }

    override fun GET(url: String, vararg parameters: RequestElement) = save(RequestData(url, "GET")) { url.GET(*parameters, ignoreStatusCode = true) }
    override fun POST(url: String, vararg parameters: RequestElement) = save(RequestData(url, "POST")) { url.POST(*parameters, ignoreStatusCode = true) }
    override fun PUT(url: String, vararg parameters: RequestElement) = save(RequestData(url, "PUT")) { url.PUT(*parameters, ignoreStatusCode = true) }
    override fun HEAD(url: String, vararg parameters: RequestElement) = save(RequestData(url, "HEAD")) { url.HEAD(*parameters, ignoreStatusCode = true) }
    override fun OPTIONS(url: String, vararg parameters: RequestElement) = save(RequestData(url, "OPTIONS")) { url.OPTIONS(*parameters, ignoreStatusCode = true) }
    override fun PATCH(url: String, vararg parameters: RequestElement) = save(RequestData(url, "PATCH")) { url.PATCH(*parameters, ignoreStatusCode = true) }
    override fun DELETE(url: String, vararg parameters: RequestElement) = save(RequestData(url, "DELETE")) { url.DELETE(*parameters, ignoreStatusCode = true) }

}
