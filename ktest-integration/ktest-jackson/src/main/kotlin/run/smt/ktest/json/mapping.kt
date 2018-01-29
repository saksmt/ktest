package run.smt.ktest.json

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.jayway.jsonpath.*
import run.smt.ktest.util.reflection.canBeAssignedTo
import run.smt.ktest.util.resource.load
import java.io.InputStream
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.KClass

val mapper = jacksonObjectMapper()

inline fun <reified T : Any> String.loadAsJson(): T = load().deserialize()
fun <T : Any> String.loadAsJson(type: JavaType): T = load() deserialize type
fun <T : Any> String.loadAsJson(type: TypeReference<T>) = load() deserialize type
fun <T : Any> String.loadAsJson(type: TypeDSL<T>) = load().deserialize(type)
fun String.loadAsJsonTree() = load().deserialize<JsonNode>()

fun Any?.serialize(): ByteArray = mapper.writeValueAsBytes(this)
fun Any?.dump(): String = mapper.writer().withDefaultPrettyPrinter().writeValueAsString(this)

inline fun <reified R : Any> String.deserialize() = this deserialize R::class.java
inline fun <reified R : Any> ByteArray.deserialize() = this deserialize R::class.java
inline fun <reified R : Any> InputStream.deserialize() = this deserialize R::class.java

infix fun <R : Any> String.deserialize(clazz: KClass<R>) = mapper.readTree(this) mapTo clazz.java
infix fun <R : Any> ByteArray.deserialize(clazz: KClass<R>) = mapper.readTree(this) mapTo clazz.java
infix fun <R : Any> InputStream.deserialize(clazz: KClass<R>) = mapper.readTree(this) mapTo clazz.java

infix fun <R : Any> String.deserialize(typeDSL: TypeDSL<R>): R = this deserialize type(typeDSL)
infix fun <R : Any> ByteArray.deserialize(typeDSL: TypeDSL<R>): R = this deserialize type(typeDSL)
infix fun <R : Any> InputStream.deserialize(typeDSL: TypeDSL<R>): R = this deserialize type(typeDSL)

infix fun <R : Any> String.deserialize(clazz: Class<R>) = mapper.readTree(this) mapTo clazz
infix fun <R : Any> ByteArray.deserialize(clazz: Class<R>) = mapper.readTree(this) mapTo clazz
infix fun <R : Any> InputStream.deserialize(clazz: Class<R>) = mapper.readTree(this) mapTo clazz

infix fun <R : Any> String.deserialize(type: JavaType) = mapper.readValue<R>(this, type)
infix fun <R : Any> ByteArray.deserialize(type: JavaType) = mapper.readValue<R>(this, type)
infix fun <R : Any> InputStream.deserialize(type: JavaType) = mapper.readValue<R>(this, type)

infix fun <R : Any> String.deserialize(type: TypeReference<R>) = mapper.readValue<R>(this, type)
infix fun <R : Any> ByteArray.deserialize(type: TypeReference<R>) = mapper.readValue<R>(this, type)
infix fun <R : Any> InputStream.deserialize(type: TypeReference<R>) = mapper.readValue<R>(this, type)

infix fun <R: Any> JsonNode.mapTo(type: TypeReference<R>): R = mapTo(mapper.constructType(type.type))
infix fun <R: Any> JsonNode.mapTo(resultClass: Class<R>): R = mapTo { simple(resultClass) }
infix fun <R: Any> JsonNode.mapTo(resultClass: KClass<R>): R = mapTo { simple(resultClass) }
infix fun <R: Any> JsonNode.mapTo(type: TypeDSL<R>): R = mapTo(type(type))

infix fun <R> JsonNode.mapTo(type: JavaType): R {
    @Suppress("UNCHECKED_CAST")
    return when {
        // TODO: not an optimal way... need to find better one
        DocumentContext::class canBeAssignedTo type.rawClass -> JsonPath.parse(this.toString()) as R
        JsonNode::class canBeAssignedTo type.rawClass -> this as R
        else -> mapper.convertValue(this, type)
    }
}

fun createJsonNode(value: Any?): JsonNode {
    val factory = mapper.nodeFactory
    return when (value) {
        null -> factory.nullNode()
        is Long -> factory.numberNode(value)
        is Short -> factory.numberNode(value)
        is Int -> factory.numberNode(value)
        is Float -> factory.numberNode(value)
        is Double -> factory.numberNode(value)
        is BigDecimal -> factory.numberNode(value)
        is BigInteger -> factory.numberNode(value)
        is Byte -> factory.numberNode(value)
        is String -> factory.textNode(value)
        is ByteArray -> factory.binaryNode(value)
        is Boolean -> factory.booleanNode(value)
        is Iterable<*> -> factory.arrayNode().addAll(value.map { createJsonNode(it) })
        is Map<*, *> -> factory.objectNode().setAll(value.mapKeys { it.key?.toString() }.mapValues { createJsonNode(it.value) })
        else -> value.serialize() deserialize JsonNode::class
    }
}
