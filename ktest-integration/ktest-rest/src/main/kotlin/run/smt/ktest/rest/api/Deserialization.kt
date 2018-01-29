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
    fun <T : Any> ResponseBodyExtractionOptions.`as`(type: JavaType): T {
        val mainType = type.rawClass
        return when {
            mainType.canBeAssignedTo<InputStream>() -> this.asInputStream() as T
            mainType.canBeAssignedTo<String>() -> this.asString() as T
            mainType.canBeAssignedTo<ByteArray>() -> this.asByteArray() as T
            mainType.canBeAssignedTo<JsonPath>() -> this.jsonPath() as T
            mainType.canBeAssignedTo<XmlPath>() -> this.xmlPath() as T
            mainType.canBeAssignedTo<JsonNode>() -> this.asJsonTree() as T
            mainType.canBeAssignedTo<Response>() -> this as T
            mainType.canBeAssignedTo<DocumentContext>() -> (asInputStream() deserialize DocumentContext::class) as T
            mainType.canBeAssignedTo<Pair<*, *>>() -> {
                val (l, r) = type.bindings.typeParameters
                if (l.rawClass.canBeAssignedTo<Int>()) {
                    Pair<Int, Any?>((this as Response).statusCode, `as`(r)) as T
                } else {
                    this.`as`(type.rawClass) as T
                }
            }
            else -> this.`as`(type.rawClass) as T
        }
    }

    fun <T : Any> ResponseBodyExtractionOptions.`as`(clazz: KClass<T>): T = `as` { simple(clazz) }
    fun <T : Any> ResponseBodyExtractionOptions.`as`(type: TypeDSL<T>): T = `as`(run.smt.ktest.json.type(type))

    fun ResponseBodyExtractionOptions.asJsonTree(): JsonNode = this.asInputStream().deserialize()
}
