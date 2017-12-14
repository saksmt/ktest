package run.smt.ktest.util.reflection

import java.lang.reflect.Method
import java.lang.reflect.Proxy
import kotlin.reflect.KClass
import java.lang.reflect.Array as ReflectArray

inline fun <reified T : Annotation> a(valueArg: Any?): T = a(T::class, valueArg)
inline fun <reified T : Annotation> a(args: Map<String, Any?>): T = a(T::class, args)
inline fun <reified T : Annotation> a(vararg args: Pair<String, Any?> = emptyArray()): T = a(mapOf(*args))

fun <T : Annotation> a(clazz: KClass<T>, args: Map<String, Any?>) = createAnnotationStub(
    clazz,
    { args.containsKey(it?.name) },
    { _, method, _ -> args[method?.name] }
)

fun <T: Annotation> a(clazz: KClass<T>, valueArg: Any?): T = createAnnotationStub(
    clazz,
    { it?.name == "value" },
    { _, _, _ -> valueArg }
)

@Suppress("UNCHECKED_CAST")
private inline fun <T : Annotation> createAnnotationStub(
    clazz: KClass<T>,
    crossinline handledCase: (Method?) -> Boolean,
    crossinline handler: (T, Method?, Array<Any?>?) -> Any?
): T {
    return Proxy.newProxyInstance(clazz.java.classLoader, arrayOf(clazz.java)) { proxy, method, args: Array<Any?>? ->
        val self = proxy as T
        handleCommonAnnotationMethods(clazz, self, method, args) ?:
            if (handledCase(method)) {
                val result = handler(self, method, args)
                val methodReturn = method?.returnType
                if (methodReturn?.isArray == true && result?.javaClass != methodReturn && result != null) {
                    val arrayResult = ReflectArray.newInstance(methodReturn.componentType, 1)
                    ReflectArray.set(arrayResult, 0, result)
                    arrayResult
                } else {
                    result
                }
            } else {
                extractDefaultsIfAny(method)
            }
    } as T
}

private fun extractDefaultsIfAny(method: Method): Any? {
    return when {
        method.defaultValue != null -> method.defaultValue
        method.returnType.isArray -> ReflectArray.newInstance(method.returnType.componentType, 0)
        else -> null
    }
}

private fun <T : Annotation> handleCommonAnnotationMethods(clazz: KClass<T>, instance: T, currentMethod: Method?, args: Array<Any?>?)
    = when (currentMethod?.name) {
    "annotationType" -> clazz.java
    "equals" -> {
        val compareTo = args?.firstOrNull()
        compareTo != null && compareTo is Annotation && extractAnnotationValues(instance).toList() == extractAnnotationValues(compareTo).toList()
    }
    "toString" -> "@" + clazz.java.canonicalName + "(" + extractAnnotationValues(instance).map { it.first + "=" + it.second }.joinToString(", ") + ")"
    "hashCode" -> instance.toString().hashCode()
    else -> null
}

private fun extractAnnotationValues(annotation: Annotation): Sequence<Pair<String, Any?>> {
    return annotation.annotationClass.java.declaredMethods.asSequence()
        .filter { it.name !in arrayOf("equals", "hashCode", "toString", "annotationType") }
        .filter { it.parameterTypes.isEmpty() }
        .map({ it.name to it.invoke(annotation) })
}
