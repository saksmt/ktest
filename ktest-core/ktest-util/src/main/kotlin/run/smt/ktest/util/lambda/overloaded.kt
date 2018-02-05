package run.smt.ktest.util.lambda

class SingleArgumentLambda<in T, out R> internal constructor(private val original: (T?) -> R) {
    operator fun invoke(param: T) = original(param)
    operator fun invoke() = original(null)
}

class DualArgumentLambda<in A, in B, out R> internal constructor(private val original: (A?, B?) -> R) {
    operator fun invoke() = original(null, null)
    operator fun invoke(a: A) = original(a, null)
    operator fun invoke(a: A?, b: B) = original(a, b)
}

fun <T, R> ((T?) -> R).overloaded() = SingleArgumentLambda(this)
fun <A, B, R> ((A?, B?) -> R).overloaded() = DualArgumentLambda(this)
