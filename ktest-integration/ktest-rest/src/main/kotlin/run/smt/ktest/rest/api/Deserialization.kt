package run.smt.ktest.rest.api

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonNode
import com.jayway.jsonpath.DocumentContext
import io.restassured.path.json.JsonPath
import io.restassured.path.xml.XmlPath
import io.restassured.response.Response
import io.restassured.response.ResponseBodyExtractionOptions
import run.smt.ktest.json.TypeDSL
import run.smt.ktest.json.deserialize
import run.smt.ktest.util.reflection.canBeAssignedTo
import java.io.InputStream
import kotlin.reflect.KClass

/**
 * Deserialization part of REST DSL
 */
interface Deserialization {
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> ResponseBodyExtractionOptions.`as`(clazz: KClass<T>): T {
        return when {
            clazz.canBeAssignedTo<InputStream>() -> this.asInputStream() as T
            clazz.canBeAssignedTo<String>() -> this.asString() as T
            clazz.canBeAssignedTo<ByteArray>() -> this.asByteArray() as T
            clazz.canBeAssignedTo<JsonPath>() -> this.jsonPath() as T
            clazz.canBeAssignedTo<XmlPath>() -> this.xmlPath() as T
            clazz.canBeAssignedTo<JsonNode>() -> this.asJsonTree() as T
            clazz.canBeAssignedTo<Response>() -> this as T
            clazz.canBeAssignedTo<DocumentContext>() -> (asInputStream() deserialize DocumentContext::class) as T
            else -> this.`as`(clazz.java)
        }
    }

    fun <T : Any> ResponseBodyExtractionOptions.`as`(type: JavaType): T = asInputStream() deserialize type
    fun <T : Any> ResponseBodyExtractionOptions.`as`(type: TypeDSL): T = asInputStream() deserialize type

    fun ResponseBodyExtractionOptions.asJsonTree(): JsonNode = this.asInputStream().deserialize()
}
