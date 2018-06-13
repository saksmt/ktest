package run.smt.ktest.util.functional.Try

fun <T : Any> success(value: T): Try<T> = Success(value)

sealed class Try<out T : Any>(
    open val value: T?,
    open val exception: Throwable?
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

    fun filter(throwIfFailed: (T) -> Throwable = { NoSuchElementException() }, predicate: (T) -> Boolean): Try<T> =
        flatMap { if (predicate(it)) Success(it) else Failure<T>(throwIfFailed(it)) }

    fun <U : Any> mapTry(mapper: (T) -> U): Try<U> = flatMap { of { mapper(it) } }
}

fun <T : Any> Try<T>.fold(ifError: (Throwable) -> T): T = when (this) {
    is Success -> value
    is Failure -> ifError(exception)
}

fun <T : Any> Try<T>.recover(recoverF: (Throwable) -> T): Try<T> = when (this) {
    is Success -> this
    is Failure -> Try.of { recoverF(exception) }
}

data class Success<out T : Any>(override val value: T) : Try<T>(value, null)
data class Failure<out T : Any>(override val exception: Throwable) : Try<T>(null, exception)
