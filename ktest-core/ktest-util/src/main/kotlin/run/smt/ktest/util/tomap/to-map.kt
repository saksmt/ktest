package run.smt.ktest.util.tomap

import java.lang.reflect.Field

/**
 * Convert any POJO to map
 */
fun <T : Any> T.toMap(): Map<String, *> {
    val classType: Class<*> = this.javaClass

    return collectFields(classType)
        .associate {
            it.name to it.extractFrom(this)
        }
}

fun <T: Any> T.toMap(deep: Boolean): Map<String, *> {
    if (!deep) {
        return toMap()
    }
    return toMap().mapValues {
        if (it.value == null || it.value is Number || it.value is Iterable<*> || it.value is Iterator<*> || it.value is Map<*, *> || it.value is String) {
            it.value
        } else {
            it.value!!.toMap(deep = true)
        }
    }
}

private fun <T> Field.extractFrom(obj: T): Any? {
    isAccessible = true
    return get(obj)
}

private fun <T> collectFields(clazz: Class<T>): Array<Field> =
    if (clazz != Any::class.java) {
        collectFields(clazz.superclass)
    } else {
        emptyArray()
    } + clazz.declaredFields
