package run.smt.ktest.internal

interface Boxed {
    operator fun invoke()
}

private class SingleExecution(body: () -> Unit) : Boxed {
    private val wrapped = lazy(body)

    override fun invoke() = wrapped.value
}

private class NormalExecution(private val body: () -> Unit) : Boxed {
    override fun invoke() = body()
}

internal object Noop : Boxed {
    override fun invoke() {}
}

internal fun List<Interceptor>.execBefore() = forEach { it.before() }
internal fun List<Interceptor>.execAfter() = reversed().forEach { it.after() }

internal fun executeOnce(body: () -> Unit): Boxed = SingleExecution(body)
internal fun executeEveryTime(body: () -> Unit): Boxed = NormalExecution(body)

data class Interceptor(val before: Boxed = Noop, val after: Boxed = Noop)
