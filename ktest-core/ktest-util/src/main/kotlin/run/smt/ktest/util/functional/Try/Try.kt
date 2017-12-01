package run.smt.ktest.util.functional.Try

sealed class Try<out T : Any>(
    val value: T?,
    val exception: Throwable?
) {
    companion object {
        fun <T : Any> of(f: () -> T): Try<T> {
            return try {
                Success(f())
            } catch (e: Throwable) {
                Failure(e)
            }
        }
    }

    fun <U : Any> map(mapper: (T) -> U): Try<U> =
        value?.let(mapper)?.let { Success(it) }
            ?: Failure(exception ?: IllegalStateException())

    fun <U : Any> flatMap(mapper: (T) -> Try<U>): Try<U> =
        value?.let(mapper) ?: Failure(exception ?: IllegalStateException())

    fun <U : Any> mapTry(mapper: (T) -> U): Try<U> = flatMap { of { mapper(it) } }
}

class Success<out T : Any> internal constructor(value: T) : Try<T>(value, null)
class Failure<out T : Any> internal constructor(exception: Throwable) : Try<T>(null, exception)
