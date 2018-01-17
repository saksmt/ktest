package run.smt.ktest.util.delegates

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <T> threadLocal(initialValue: () -> T): ReadWriteProperty<Any?, T> = ThreadLocalProperty(initialValue)
fun <T> threadLocal(initialValue: T): ReadWriteProperty<Any?, T> = ThreadLocalProperty { initialValue }

fun <T> inheritableThreadLocal(initialValue: () -> T): ReadWriteProperty<Any?, T> = InheritableThreadLocalProperty(initialValue)
fun <T> inheritableThreadLocal(initialValue: T): ReadWriteProperty<Any?, T> = InheritableThreadLocalProperty { initialValue }

private class ThreadLocalProperty<T>(initialValue: () -> T) : ReadWriteProperty<Any?, T> {
    private val prop = object : ThreadLocal<T>() {
        override fun initialValue(): T {
            return initialValue()
        }
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = prop.get()

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = prop.set(value)
}

private class InheritableThreadLocalProperty<T>(initialValue: () -> T) : ReadWriteProperty<Any?, T> {
    private val prop = object : InheritableThreadLocal<T>() {
        override fun initialValue(): T {
            return initialValue()
        }
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T = prop.get()

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = prop.set(value)
}
