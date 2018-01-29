package run.smt.ktest.json

import com.fasterxml.jackson.databind.JavaType
import kotlin.reflect.KClass

typealias TypeDSL<T> = TypeBuilder.() -> Typed<T>

@Suppress("unused")
class Typed<out T> internal constructor(internal val asJavaType: JavaType)

fun type(typed: Typed<*>) = typed.asJavaType
fun type(dsl: TypeDSL<*>) = type(TypeBuilder.dsl())

object TypeBuilder {
    private val typeFactory = mapper.typeFactory

    inline fun <reified T : Any> list() = list(T::class)
    fun <T : Any> list(clazz: KClass<T>) = list(clazz.java)
    fun <T : Any> list(clazz: Class<T>): Typed<List<T>> = Typed(typeFactory.constructCollectionType(List::class.java, clazz))
    fun list(type: JavaType): Typed<List<*>> = Typed(typeFactory.constructCollectionType(List::class.java, type))
    fun <T : Any> list(type: Typed<T>): Typed<List<T>> = Typed(typeFactory.constructCollectionType(List::class.java, type.asJavaType))

    inline fun <reified T : Any> set() = set(T::class)
    fun <T : Any> set(clazz: KClass<T>) = set(clazz.java)
    fun <T : Any> set(clazz: Class<T>): Typed<Set<T>> = Typed(typeFactory.constructCollectionType(Set::class.java, clazz))
    fun set(type: JavaType): Typed<Set<*>> = Typed(typeFactory.constructCollectionType(Set::class.java, type))
    fun <T: Any> set(type: Typed<T>): Typed<Set<T>> = Typed(typeFactory.constructCollectionType(Set::class.java, type.asJavaType))

    inline fun <reified K : Any, reified V : Any> map() = map(K::class, V::class)
    fun <K : Any, V : Any> map(key: KClass<K>, value: KClass<V>) = map(key.java, value.java)
    fun <K : Any, V : Any> map(key: Class<K>, value: KClass<V>) = map(key, value.java)
    fun <K : Any, V : Any> map(key: KClass<K>, value: Class<V>) = map(key.java, value)
    fun <K : Any, V : Any> map(key: Class<K>, value: Class<V>): Typed<Map<K, V>> = Typed(typeFactory.constructMapType(Map::class.java, key, value))
    fun <K : Any, V : Any> map(key: Typed<K>, value: Typed<V>): Typed<Map<K, V>> = Typed(typeFactory.constructMapType(Map::class.java, key.asJavaType, value.asJavaType))
    fun map(key: JavaType, value: JavaType): Typed<Map<*, *>> = Typed(typeFactory.constructMapType(Map::class.java, key, value))

    fun <T : Any> generic(clazz: KClass<*>, vararg parameters: Class<*>): Typed<T> = generic(clazz.java, *parameters)
    fun <T : Any> generic(clazz: KClass<*>, vararg parameters: KClass<*>): Typed<T> = generic(clazz.java, *parameters)
    fun <T : Any> generic(clazz: KClass<*>, vararg parameters: JavaType): Typed<T> = generic(clazz.java, *parameters)
    fun <T : Any> generic(clazz: Class<*>, vararg parameters: KClass<*>): Typed<T> = generic(clazz, *parameters.map(KClass<*>::java).toTypedArray())
    fun <T : Any> generic(clazz: KClass<*>, vararg parameters: Typed<*>): Typed<T> = generic(clazz.java, *parameters)
    fun <T : Any> generic(clazz: Class<*>, vararg parameters: Class<*>): Typed<T> = Typed(typeFactory.constructParametricType(clazz, *parameters))
    fun <T : Any> generic(clazz: Class<*>, vararg parameters: JavaType): Typed<T> = Typed(typeFactory.constructParametricType(clazz, *parameters))
    fun <T : Any> generic(clazz: Class<*>, vararg parameters: Typed<*>): Typed<T> = Typed(typeFactory.constructParametricType(clazz, *parameters.map(Typed<*>::asJavaType).toTypedArray()))

    inline fun <reified T : Any> simple() = simple(T::class)
    fun <T : Any> simple(clazz: KClass<T>) = simple(clazz.java)
    fun <T : Any> simple(clazz: Class<T>): Typed<T> = Typed(typeFactory.constructSimpleType(clazz, clazz.typeParameters.map { simple<Any>().asJavaType }.toTypedArray()))


    inline fun <reified K : Any, reified V : Any> pair() = pair(K::class, V::class)
    fun <K : Any, V : Any> pair(key: KClass<K>, value: KClass<V>) = pair(key.java, value.java)
    fun <K : Any, V : Any> pair(key: Class<K>, value: KClass<V>) = pair(key, value.java)
    fun <K : Any, V : Any> pair(key: KClass<K>, value: Class<V>) = pair(key.java, value)
    fun <K : Any, V : Any> pair(key: Class<K>, value: Class<V>): Typed<Pair<K, V>> = Typed(type { generic<Pair<K, V>>(Pair::class, key, value) })
    fun <K : Any, V : Any> pair(key: Typed<K>, value: Typed<V>): Typed<Pair<K, V>> = Typed(type { generic<Pair<K, V>>(Pair::class, key, value) })
    fun pair(key: JavaType, value: JavaType): Typed<Pair<*, *>> = Typed(type { generic<Pair<*, *>>(Pair::class, key, value) })
}
