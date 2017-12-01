package run.smt.ktest.db.registry

import run.smt.ktest.util.reflection.canBeAssignedTo
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import kotlin.reflect.KClass

/**
 * Represents test resources that must be cleanly loaded into some storage
 */
abstract class TestDataRegistry {
    private val registry: ConcurrentMap<String, Any> = ConcurrentHashMap()

    abstract fun <T : Any> load(clazz: KClass<T>, identifier: String): T?
    abstract fun <T : Any> loadAll(clazz: KClass<T>, identifier: String): List<T>
    protected abstract fun <T : Any> save(data: T)
    protected abstract fun <T : Any> remove(data: T)

    /**
     * Drop previous values, load fresh once and return result
     */
    inline operator fun <reified T : Any> get(identifier: String): T = get(T::class, identifier)

    inline fun <reified T : Any> getAll(identifier: String): List<T> = getAll(T::class, identifier)

    /**
     * Same as `get` but without returning
     */
    inline fun <reified T : Any> setup(identifier: String) {
        get<T>(identifier)
    }

    inline fun <reified T : Any> setupAll(identifier: String) {
        getAll<T>(identifier)
    }

    /**
     * Only return value extracted from resources
     */
    inline fun <reified T : Any> load(identifier: String): T? = load(T::class, identifier)

    inline fun <reified T : Any> loadAll(identifier: String): List<T> = loadAll(T::class, identifier)

    fun <T : Any> get(clazz: KClass<T>, identifier: String): T {
        val result = registry.computeIfAbsent(identifier) {
            val loaded = load(clazz, it) ?: throw NoValueException("Can not load value with identifier \"$identifier\"")

            remove(loaded)
            save(loaded)

            loaded
        }

        if (result.javaClass canBeAssignedTo clazz) {
            @Suppress("UNCHECKED_CAST") return result as T
        }

        throw NoValueException("Requested type for element with identifier \"$identifier\" don't match actual!")
    }

    fun <T : Any> getAll(clazz: KClass<T>, identifier: String): List<T> {
        val result = registry.computeIfAbsent(identifier) {
            val loaded = loadAll(clazz, it)

            loaded.forEach {
                remove(it)
                save(it)
            }

            loaded
        }

        return result as? List<T> ?: throw NoValueException("Requested type for element with identifier \"$identifier\" don't match actual!")
    }

    companion object {
        class NoValueException(message: String) : RuntimeException(message)
        class SaveException(message: String) : RuntimeException(message)
    }
}
