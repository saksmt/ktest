package run.smt.ktest.util.loader

import run.smt.ktest.util.collection.padTo
import run.smt.ktest.util.functional.Try.Try
import run.smt.ktest.util.reflection.canBeAssignedTo
import java.lang.reflect.Parameter
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.jvmErasure

typealias ClassName = String

enum class InjectionMode {
    STRICT,
    ALLOW_EXTRA_ARGS
}

inline fun <reified T : Any> loadClass(name: ClassName): Try<KClass<T>> {
    val unsupportedClass: (KClass<*>) -> IllegalArgumentException = {
        IllegalArgumentException("Failed to create instance of ${T::class.simpleName}. " +
            "Provided class ($name) is outside of hierarchy")
    }
    return Try.of {
        Class.forName(name)
    }.map {
        it.kotlin
    }.filter(throwIfFailed = unsupportedClass) {
        it canBeAssignedTo T::class || it.companionObject?.let { it::class canBeAssignedTo T::class } ?: false
    }.map {
        @Suppress("UNCHECKED_CAST")
        it as KClass<T>
    }
}

inline fun <reified T : Any> load(what: ClassName, vararg args: Any?) = load<T>(what = what, injectionMode = InjectionMode.ALLOW_EXTRA_ARGS, args = *args)
inline fun <reified T : Any> load(what: ClassName, injectionMode: InjectionMode, vararg args: Any?): Try<T> {
    return loadClass<T>(what).mapTry(instantiate(injectionMode, *args))
}

inline fun <reified T : Any> instantiate(vararg args: Any?) = instantiate<T>(injectionMode = InjectionMode.ALLOW_EXTRA_ARGS, args = *args)
inline fun <reified T : Any> instantiate(injectionMode: InjectionMode, vararg args: Any?): (KClass<out T>) -> T {
    return instantiate(T::class, injectionMode, *args)
}

fun <T : Any> instantiate(targetType: KClass<T>, injectionMode: InjectionMode = InjectionMode.ALLOW_EXTRA_ARGS, vararg args: Any?): (KClass<out T>) -> T {
    return { clazz ->
        if (clazz.objectInstance?.let { it::class canBeAssignedTo targetType } == true) {
            @Suppress("UNCHECKED_CAST")
            clazz.objectInstance as T
        } else {
            clazz.java.constructors.asSequence()
                .sortedBy { it.isAccessible }
                .flatMap { ctor ->
                    possibleInjections(injectionMode, ctor.parameters.toList(), args.toList()).asSequence()
                        .map { args -> {
                            ctor.isAccessible = true
                            ctor.newInstance(*args.toTypedArray()) as T
                        } }
                }
                .firstOrNull()?.invoke() ?: throw IllegalArgumentException("No matching constructor found for $clazz")
        }
    }
}

private fun possibleInjections(injectionMode: InjectionMode, formal: List<Parameter>, actual: List<Any?>): List<List<Any?>> {
    var index = 0
    while (true) {
        val formalArg = formal.getOrNull(index)
        val actualArg = actual.getOrNull(index)

        if (formal.size <= index) {
            return if (injectionMode == InjectionMode.STRICT && actual.size > index) {
                emptyList()
            } else {
                listOf(actual.take(index))
            }
        }


        if (actual.size <= index) {
            return emptyList()
        }

        index++

        if (formalArg?.type?.kotlin?.isInstance(actualArg) != true) {
            return emptyList()
        }

        if (formal.size < index && actual.size < index) {
            throw IllegalStateException("Author of ktest is an idiot. You should create an issue for this...")
        }
    }
}
