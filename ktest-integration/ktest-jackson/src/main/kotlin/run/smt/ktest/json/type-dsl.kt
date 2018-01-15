package run.smt.ktest.json

import com.fasterxml.jackson.databind.JavaType
import java.lang.reflect.Type
import kotlin.reflect.KClass

typealias TypeDSL = TypeBuilder.() -> JavaType

fun type(dsl: TypeDSL) = TypeBuilder.dsl()
object TypeBuilder {
    private val typeFactory = mapper.typeFactory

    inline fun <reified T : Any> list() = list(T::class)
    fun <T : Any> list(clazz: KClass<T>) = list(clazz.java)
    fun <T : Any> list(clazz: Class<T>): JavaType = typeFactory.constructCollectionType(List::class.java, clazz)
    fun list(type: JavaType): JavaType = typeFactory.constructCollectionType(List::class.java, type)

    inline fun <reified T : Any> set() = set(T::class)
    fun <T : Any> set(clazz: KClass<T>) = set(clazz.java)
    fun <T : Any> set(clazz: Class<T>): JavaType = typeFactory.constructCollectionType(Set::class.java, clazz)
    fun set(type: JavaType): JavaType = typeFactory.constructCollectionType(Set::class.java, type)

    inline fun <reified K : Any, reified V : Any> map() = map(K::class, V::class)
    fun <K : Any, V : Any> map(key: KClass<K>, value: KClass<V>) = map(key.java, value.java)
    fun <K : Any, V : Any> map(key: Class<K>, value: KClass<V>) = map(key, value.java)
    fun <K : Any, V : Any> map(key: KClass<K>, value: Class<V>) = map(key.java, value)
    fun <K : Any, V : Any> map(key: Class<K>, value: Class<V>): JavaType = typeFactory.constructMapType(Map::class.java, key, value)
    fun map(key: JavaType, value: JavaType): JavaType = typeFactory.constructMapType(Map::class.java, key, value)

    fun <T : Any> generic(clazz: KClass<T>, vararg parameters: Class<*>) = generic(clazz.java, *parameters)
    fun <T : Any> generic(clazz: KClass<T>, vararg parameters: KClass<*>) = generic(clazz.java, *parameters)
    fun <T : Any> generic(clazz: KClass<T>, vararg parameters: JavaType) = generic(clazz.java, *parameters)
    fun <T : Any> generic(clazz: Class<T>, vararg parameters: KClass<*>) = generic(clazz, *parameters.map(KClass<*>::java).toTypedArray())
    fun <T : Any> generic(clazz: Class<T>, vararg parameters: Class<*>): JavaType = typeFactory.constructParametricType(clazz, *parameters)
    fun <T : Any> generic(clazz: Class<T>, vararg parameters: JavaType): JavaType = typeFactory.constructParametricType(clazz, *parameters)

    inline fun <reified T : Any> simple() = simple(T::class)
    fun <T : Any> simple(clazz: KClass<T>) = simple(clazz.java)
    fun <T : Any> simple(clazz: Class<T>): JavaType = typeFactory.constructSimpleType(clazz, clazz.typeParameters.map { simple<Any>() }.toTypedArray())
}
