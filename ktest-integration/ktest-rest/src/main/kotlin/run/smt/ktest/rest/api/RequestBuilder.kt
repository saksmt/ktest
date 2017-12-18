package run.smt.ktest.rest.api

import io.restassured.response.Response

/**
 * Result entry point for REST DSL + reified shortcuts
 */
abstract class RequestBuilder :
    RequestElementBuilder,
    ComplexQueriesBuilder,
    SimpleRequests,
    Debugging,
    Deserialization {

    // Since we can not declare inline methods in interface we delegate from inline methods declared here to parent once

    inline fun <reified T : Any> String.GET(vararg parameters: RequestElement, ignoreStatusCode: Boolean = false) = GET(T::class, *parameters, ignoreStatusCode = ignoreStatusCode)
    inline fun <reified T : Any> String.PUT(vararg parameters: RequestElement, ignoreStatusCode: Boolean = false) = PUT(T::class, *parameters, ignoreStatusCode = ignoreStatusCode)
    inline fun <reified T : Any> String.POST(vararg parameters: RequestElement, ignoreStatusCode: Boolean = false) = POST(T::class, *parameters, ignoreStatusCode = ignoreStatusCode)
    inline fun <reified T : Any> String.HEAD(vararg parameters: RequestElement, ignoreStatusCode: Boolean = false) = HEAD(T::class, *parameters, ignoreStatusCode = ignoreStatusCode)
    inline fun <reified T : Any> String.PATCH(vararg parameters: RequestElement, ignoreStatusCode: Boolean = false) = PATCH(T::class, *parameters, ignoreStatusCode = ignoreStatusCode)
    inline fun <reified T : Any> String.DELETE(vararg parameters: RequestElement, ignoreStatusCode: Boolean = false) = DELETE(T::class, *parameters, ignoreStatusCode = ignoreStatusCode)
    inline fun <reified T : Any> String.OPTIONS(vararg parameters: RequestElement, ignoreStatusCode: Boolean = false) = OPTIONS(T::class, *parameters, ignoreStatusCode = ignoreStatusCode)
    inline fun <reified T : Any> Response.`as`(): T = `as`(T::class)
}
