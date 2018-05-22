package run.smt.ktest.rest.api

import com.fasterxml.jackson.databind.JavaType
import io.restassured.response.Response
import io.restassured.specification.RequestSender
import org.hamcrest.CustomTypeSafeMatcher
import run.smt.ktest.json.TypeDSL
import run.smt.ktest.json.type
import kotlin.reflect.KClass

/**
 * COPY-PASTE avoidance
 */
private inline fun <T : Any> SimpleRequests.request(clazz: KClass<T>, parameters: Array<out RequestElement>, ignoreStatusCode: Boolean, query: RequestSender.() -> Response): T {
    return request(parameters, ignoreStatusCode, query).`as`(clazz)
}

private inline fun <T : Any> SimpleRequests.request(clazz: JavaType, parameters: Array<out RequestElement>, ignoreStatusCode: Boolean, query: RequestSender.() -> Response): T {
    return request(parameters, ignoreStatusCode, query).`as`(clazz)
}


private inline fun SimpleRequests.request(parameters: Array<out RequestElement>, ignoreStatusCode: Boolean, query: RequestSender.() -> Response): Response {
    return request(parameters.asSequence())
        .expect()
        .apply {
            if (!ignoreStatusCode) {
                statusCode(object : CustomTypeSafeMatcher<Int>("status code is successful") {
                    override fun matchesSafely(statusCode: Int?) = statusCode != null && statusCode >= 200 && statusCode < 400
                })
            }
        }
        .`when`()
        .query().andReturn()
}

/**
 * Part of REST DSL for performing simple requests with automatic deserialization to requested type + check for status code
 * to be successful
 */
interface SimpleRequests : Deserialization, ComplexQueriesBuilder {
    // Plain reflection responses

    fun <T : Any> String.GET(clazz: KClass<T>, vararg parameters: RequestElement, ignoreStatusCode: Boolean = false): T =
        request(clazz, parameters, ignoreStatusCode) { get(this@GET) }

    fun <T : Any> String.POST(clazz: KClass<T>, vararg parameters: RequestElement, ignoreStatusCode: Boolean = false): T =
        request(clazz, parameters, ignoreStatusCode) { post(this@POST) }

    fun <T : Any> String.PUT(clazz: KClass<T>, vararg parameters: RequestElement, ignoreStatusCode: Boolean = false): T =
        request(clazz, parameters, ignoreStatusCode) { put(this@PUT) }

    fun <T : Any> String.HEAD(clazz: KClass<T>, vararg parameters: RequestElement, ignoreStatusCode: Boolean = false): T =
        request(clazz, parameters, ignoreStatusCode) { head(this@HEAD) }

    fun <T : Any> String.OPTIONS(clazz: KClass<T>, vararg parameters: RequestElement, ignoreStatusCode: Boolean = false): T =
        request(clazz, parameters, ignoreStatusCode) { options(this@OPTIONS) }

    fun <T : Any> String.PATCH(clazz: KClass<T>, vararg parameters: RequestElement, ignoreStatusCode: Boolean = false): T =
        request(clazz, parameters, ignoreStatusCode) { patch(this@PATCH) }

    fun <T : Any> String.DELETE(clazz: KClass<T>, vararg parameters: RequestElement, ignoreStatusCode: Boolean = false): T =
        request(clazz, parameters, ignoreStatusCode) { delete(this@DELETE) }


    // Jackson's JavaTypes

    fun <T : Any> String.GET(clazz: JavaType, vararg parameters: RequestElement, ignoreStatusCode: Boolean = false): T =
        request(clazz, parameters, ignoreStatusCode) { get(this@GET) }

    fun <T : Any> String.POST(clazz: JavaType, vararg parameters: RequestElement, ignoreStatusCode: Boolean = false): T =
        request(clazz, parameters, ignoreStatusCode) { post(this@POST) }

    fun <T : Any> String.PUT(clazz: JavaType, vararg parameters: RequestElement, ignoreStatusCode: Boolean = false): T =
        request(clazz, parameters, ignoreStatusCode) { put(this@PUT) }

    fun <T : Any> String.HEAD(clazz: JavaType, vararg parameters: RequestElement, ignoreStatusCode: Boolean = false): T =
        request(clazz, parameters, ignoreStatusCode) { head(this@HEAD) }

    fun <T : Any> String.OPTIONS(clazz: JavaType, vararg parameters: RequestElement, ignoreStatusCode: Boolean = false): T =
        request(clazz, parameters, ignoreStatusCode) { options(this@OPTIONS) }

    fun <T : Any> String.PATCH(clazz: JavaType, vararg parameters: RequestElement, ignoreStatusCode: Boolean = false): T =
        request(clazz, parameters, ignoreStatusCode) { patch(this@PATCH) }

    fun <T : Any> String.DELETE(clazz: JavaType, vararg parameters: RequestElement, ignoreStatusCode: Boolean = false): T =
        request(clazz, parameters, ignoreStatusCode) { delete(this@DELETE) }


    // kTest JSON TypeDSLs

    fun <T : Any> String.GET(vararg parameters: RequestElement, ignoreStatusCode: Boolean = false, builder: TypeDSL<T>): T =
        GET(clazz = type(builder), parameters = *parameters, ignoreStatusCode = ignoreStatusCode)

    fun <T : Any> String.POST(vararg parameters: RequestElement, ignoreStatusCode: Boolean = false, builder: TypeDSL<T>): T =
        POST(clazz = type(builder), parameters = *parameters, ignoreStatusCode = ignoreStatusCode)

    fun <T : Any> String.PUT(vararg parameters: RequestElement, ignoreStatusCode: Boolean = false, builder: TypeDSL<T>): T =
        PUT(clazz = type(builder), parameters = *parameters, ignoreStatusCode = ignoreStatusCode)

    fun <T : Any> String.HEAD(vararg parameters: RequestElement, ignoreStatusCode: Boolean = false, builder: TypeDSL<T>): T =
        HEAD(clazz = type(builder), parameters = *parameters, ignoreStatusCode = ignoreStatusCode)

    fun <T : Any> String.OPTIONS(vararg parameters: RequestElement, ignoreStatusCode: Boolean = false, builder: TypeDSL<T>): T =
        OPTIONS(clazz = type(builder), parameters = *parameters, ignoreStatusCode = ignoreStatusCode)

    fun <T : Any> String.PATCH(vararg parameters: RequestElement, ignoreStatusCode: Boolean = false, builder: TypeDSL<T>): T =
        PATCH(clazz = type(builder), parameters = *parameters, ignoreStatusCode = ignoreStatusCode)

    fun <T : Any> String.DELETE(vararg parameters: RequestElement, ignoreStatusCode: Boolean = false, builder: TypeDSL<T>): T =
        DELETE(clazz = type(builder), parameters = *parameters, ignoreStatusCode = ignoreStatusCode)
}
