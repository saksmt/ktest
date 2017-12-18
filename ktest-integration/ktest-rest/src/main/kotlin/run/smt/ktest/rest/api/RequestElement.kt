package run.smt.ktest.rest.api

import com.fasterxml.jackson.databind.JsonNode

/**
 * ADT for possible request elements
 */
sealed class RequestElement {
    fun flatten(): List<RequestElement> = if (this is CompositeParameter) elements.flatMap { it.flatten() } else listOf(this)

    class Header internal constructor(val name: String, val value: String) : RequestElement()
    class QueryParameter internal constructor(val name: String, val value: Any?) : RequestElement()
    class Body internal constructor(val data: Any?) : RequestElement()
    class PathParameter internal constructor(val name: String, val value: String) : RequestElement()

    internal class CompositeParameter(val elements: List<RequestElement>) : RequestElement()
}

/**
 * Part of REST DSL for building request elements
 */
interface RequestElementBuilder {
    fun header(header: Pair<String, String>) = header(header.first, header.second)
    fun header(name: String, value: String): RequestElement = RequestElement.Header(name, value)
    fun headers(headers: Map<String, String>): RequestElement = RequestElement.CompositeParameter(headers.map { (k, v) -> header(k, v) })

    fun queryParam(param: Pair<String, Any?>): RequestElement = queryParam(param.first, param.second)
    fun queryParam(name: String, value: Any?): RequestElement = RequestElement.QueryParameter(name, value)
    fun queryParams(params: List<Pair<String, Any?>>): RequestElement = RequestElement.CompositeParameter(params.map { (n, v) -> queryParam(n, v) })
    fun queryParams(params: Map<String, Any?>): RequestElement = RequestElement.CompositeParameter(params.map { (n, v) -> queryParam(n, v) })

    fun pathParam(pathParam: Pair<String, Any>): RequestElement = pathParams(listOf(pathParam))
    fun pathParam(name: String, value: String): RequestElement = RequestElement.PathParameter(name, value)
    fun pathParam(name: String, value: Long): RequestElement = RequestElement.PathParameter(name, value.toString())
    fun pathParam(name: String, value: Int): RequestElement = RequestElement.PathParameter(name, value.toString())
    fun pathParams(params: List<Pair<String, Any>>): RequestElement = RequestElement.CompositeParameter(params.map { (n, v) -> genericPathParam(n, v) })
    fun pathParams(params: Map<String, Any>): RequestElement = RequestElement.CompositeParameter(params.map { (n, v) -> genericPathParam(n, v) })

    fun body(content: Any?): RequestElement = if (content is JsonNode) body(content.toString()) else RequestElement.Body(content)
    fun body(vararg map: Pair<*, *>): RequestElement = RequestElement.Body(map.associate { it })
}

object DefaultRequestElementBuilder : RequestElementBuilder

private fun RequestElementBuilder.genericPathParam(name: String, value: Any): RequestElement {
    return when (value) {
        is String -> pathParam(name, value)
        is Long -> pathParam(name, value)
        is Int -> pathParam(name, value)
        else -> throw IllegalArgumentException("Value of type ${value.javaClass.canonicalName} is unsupported for path parameters!")
    }
}
