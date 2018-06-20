package run.smt.ktest.rest.impl

import io.restassured.RestAssured.config
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.config.HttpClientConfig
import io.restassured.config.LogConfig.logConfig
import io.restassured.filter.Filter
import io.restassured.http.ContentType
import io.restassured.internal.TestSpecificationImpl
import io.restassured.parsing.Parser
import io.restassured.specification.RequestSpecification
import org.apache.http.params.CoreConnectionPNames
import run.smt.ktest.rest.api.RequestBuilder
import run.smt.ktest.rest.api.RequestElement
import run.smt.ktest.rest.authorization.AuthorizationAdapter

/**
 * Used as receiver for DSL
 */
internal class RestAssuredRequestsAdapter(
    private val baseUrl: String,
    private val authorizationAdapter: AuthorizationAdapter,
    private val logger: Filter,
    private val socketTimeout: Int,
    private val connectTimeout: Int
) : RequestBuilder() {
    override var debug: Boolean = false

    override fun request(parameters: Sequence<RequestElement>): RequestSpecification {
        val enrichedParameters = authorizationAdapter.run { enrichRequest(parameters) }
        val flattenedParameters = enrichedParameters.flatMap { it.flatten().asSequence() }
        val headers = flattenedParameters
            .filterIsInstance<RequestElement.Header>()
            .groupBy { it.name.toLowerCase() }
            .mapValues { it.value.last() }
            .values
        val queryParameters = flattenedParameters
            .filterIsInstance<RequestElement.QueryParameter>()
            .groupBy({ it.name }, { it.value })
        val body = flattenedParameters.filterIsInstance<RequestElement.Body>().firstOrNull()

        // Build new rest assured request specification
        val requestBuilder = RequestSpecBuilder().apply {
            setConfig(config().logConfig(logConfig()
                .enableLoggingOfRequestAndResponseIfValidationFails()
                .enablePrettyPrinting(true)
            ))
            addFilter(logger)
            setContentType(ContentType.JSON.withCharset(Charsets.UTF_8))
            setAccept(ContentType.ANY.withCharset(Charsets.UTF_8))
            setBaseUri(baseUrl)
            headers.forEach { addHeader(it.name, it.value) }
            queryParameters.forEach { addQueryParam(it.key, it.value) }
            if (body != null) {
                setBody(body.data)
            }
            setConfig(config().httpClient(HttpClientConfig.httpClientConfig()
                .setParam(CoreConnectionPNames.CONNECTION_TIMEOUT, connectTimeout)
                .setParam(CoreConnectionPNames.SO_TIMEOUT, socketTimeout)))
        }

        val spec = TestSpecificationImpl(
            requestBuilder.build(),
            ResponseSpecBuilder().setDefaultParser(Parser.JSON).build()
        )

        if (debug) {
            spec.requestSpecification.log().everything()
            spec.responseSpecification.log().everything()
        }

        return spec.requestSpecification
    }
}
