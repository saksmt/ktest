package run.smt.ktest.json

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.jayway.jsonpath.*
import run.smt.ktest.util.reflection.canBeAssignedTo
import run.smt.ktest.util.resource.load
import run.smt.ktest.config.config
import run.smt.ktest.config.get
import run.smt.ktest.util.loader.load
import run.smt.ktest.util.functional.Try.*
import java.io.InputStream
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.KClass

private fun createMapper(): ObjectMapper {
    val configurerClasses: List<String> = config["json.configurers"]
    return configurerClasses
        .map { load<JsonConfigurer>(it).fold { throw it } }
        .fold(ObjectMapper()) { acc, v -> v(acc) }
}

val mapper by lazy { jacksonObjectMapper() }

val globalWrapped by lazy { WrappedMapper(mapper) }

inline fun <reified T : Any> String.loadAsJson(): T = with(globalWrapped) { this@loadAsJson.loadAsJson() }
fun <T : Any> String.loadAsJson(type: JavaType): T = with(globalWrapped) { this@loadAsJson.loadAsJson(type) }
fun <T : Any> String.loadAsJson(type: TypeReference<T>) = with(globalWrapped) { this@loadAsJson.loadAsJson(type) }
fun <T : Any> String.loadAsJson(type: TypeDSL<T>) = with(globalWrapped) { this@loadAsJson.loadAsJson(type) }
fun String.loadAsJsonTree() = with(globalWrapped) { this@loadAsJsonTree.loadAsJsonTree() }

fun Any?.serialize(): ByteArray = with(globalWrapped) { this@serialize.serialize() }
fun Any?.dump(): String = with(globalWrapped) { this@dump.dump() }

inline fun <reified R : Any> String.deserialize() = with(globalWrapped) { this@deserialize.deserialize<R>() }
inline fun <reified R : Any> ByteArray.deserialize() = with(globalWrapped) { this@deserialize.deserialize<R>() }
inline fun <reified R : Any> InputStream.deserialize() = with(globalWrapped) { this@deserialize.deserialize<R>() }

infix fun <R : Any> String.deserialize(clazz: KClass<R>) = with(globalWrapped) { this@deserialize.deserialize(clazz) }
infix fun <R : Any> ByteArray.deserialize(clazz: KClass<R>) = with(globalWrapped) { this@deserialize.deserialize(clazz) }
infix fun <R : Any> InputStream.deserialize(clazz: KClass<R>) = with(globalWrapped) { this@deserialize.deserialize(clazz) }

infix fun <R : Any> String.deserialize(typeDSL: TypeDSL<R>): R = with(globalWrapped) { this@deserialize.deserialize(typeDSL) }
infix fun <R : Any> ByteArray.deserialize(typeDSL: TypeDSL<R>): R = with(globalWrapped) { this@deserialize.deserialize(typeDSL) }
infix fun <R : Any> InputStream.deserialize(typeDSL: TypeDSL<R>): R = with(globalWrapped) { this@deserialize.deserialize(typeDSL) }

infix fun <R : Any> String.deserialize(clazz: Class<R>) = with(globalWrapped) { this@deserialize.deserialize(clazz) }
infix fun <R : Any> ByteArray.deserialize(clazz: Class<R>) = with(globalWrapped) { this@deserialize.deserialize(clazz) }
infix fun <R : Any> InputStream.deserialize(clazz: Class<R>) = with(globalWrapped) { this@deserialize.deserialize(clazz) }

infix fun <R : Any> String.deserialize(type: JavaType) = with(globalWrapped) { this@deserialize.deserialize<R>(type) }
infix fun <R : Any> ByteArray.deserialize(type: JavaType) = with(globalWrapped) { this@deserialize.deserialize<R>(type) }
infix fun <R : Any> InputStream.deserialize(type: JavaType) = with(globalWrapped) { this@deserialize.deserialize<R>(type) }

infix fun <R : Any> String.deserialize(type: TypeReference<R>) = with(globalWrapped) { this@deserialize.deserialize(type) }
infix fun <R : Any> ByteArray.deserialize(type: TypeReference<R>) = with(globalWrapped) { this@deserialize.deserialize(type) }
infix fun <R : Any> InputStream.deserialize(type: TypeReference<R>) = with(globalWrapped) { this@deserialize.deserialize(type) }

inline fun <reified R: Any> JsonNode.mapTo(): R = with(globalWrapped) { this@mapTo.mapTo() }

infix fun <R: Any> JsonNode.mapTo(type: TypeReference<R>): R = with(globalWrapped) { this@mapTo.mapTo(type) }
infix fun <R: Any> JsonNode.mapTo(type: TypeDSL<R>): R = with(globalWrapped) { this@mapTo.mapTo(type) }

infix fun <R: Any> JsonNode.mapTo(resultClass: Class<R>): R = with(globalWrapped) { this@mapTo.mapTo(resultClass) }
infix fun <R: Any> JsonNode.mapTo(resultClass: KClass<R>): R = with(globalWrapped) { this@mapTo.mapTo(resultClass) }

infix fun <R> JsonNode.mapTo(type: JavaType): R = with(globalWrapped) { this@mapTo.mapTo(type) }

fun createJsonNode(value: Any?): JsonNode = with(globalWrapped) { this.createJsonNode(value) }

class WrappedMapper internal constructor(private val mapper: ObjectMapper) {
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

    infix fun <R : Any> String.deserialize(type: JavaType): R = mapper.readValue<R>(this, type)
    infix fun <R : Any> ByteArray.deserialize(type: JavaType): R = mapper.readValue<R>(this, type)
    infix fun <R : Any> InputStream.deserialize(type: JavaType): R = mapper.readValue<R>(this, type)

    infix fun <R : Any> String.deserialize(type: TypeReference<R>): R = mapper.readValue<R>(this, type)
    infix fun <R : Any> ByteArray.deserialize(type: TypeReference<R>): R = mapper.readValue<R>(this, type)
    infix fun <R : Any> InputStream.deserialize(type: TypeReference<R>): R = mapper.readValue<R>(this, type)

    inline fun <reified R: Any> JsonNode.mapTo(): R = mapTo(R::class)
    infix fun <R: Any> JsonNode.mapTo(type: TypeReference<R>): R = mapTo(mapper.constructType(type.type))
    infix fun <R: Any> JsonNode.mapTo(type: TypeDSL<R>): R = mapTo(type(type))
    infix fun <R: Any> JsonNode.mapTo(resultClass: Class<R>): R = mapTo { simple(resultClass) }
    infix fun <R: Any> JsonNode.mapTo(resultClass: KClass<R>): R = mapTo { simple(resultClass) }

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
}

fun <T> with(mapper: ObjectMapper, dsl: WrappedMapper.() -> T) {
    WrappedMapper(mapper).dsl()
}
