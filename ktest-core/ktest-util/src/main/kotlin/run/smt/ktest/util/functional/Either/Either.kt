package run.smt.ktest.util.functional.Either

import run.smt.ktest.util.functional.identity

fun <A, B> left(left: A): Either<A, B> = Left(left)
fun <A, B> right(right: B): Either<A, B> = Right(right)

sealed class Either<A, B> {
    abstract val left: A?
    abstract val right: B?
    abstract val isRight: Boolean
    abstract val flip: Either<B, A>
    val isLeft: Boolean = !isRight

    inline fun <AA, BB> bimap(leftMapper: (A) -> AA, rightMapper: (B) -> BB): Either<AA, BB>
        = if (isLeft) left(leftMapper(left!!)) else right(rightMapper(right!!))

    inline fun <AA> mapLeft(mapper: (A) -> AA): Either<AA, B> = bimap(mapper, identity())
    inline fun <BB> mapRight(mapper: (B) -> BB): Either<A, BB> = bimap(identity(), mapper)

    inline fun <C> unify(leftMapper: (A) -> C, rightMapper: (B) -> C): C
        = if (isRight) rightMapper(right!!) else leftMapper(left!!)
}

private class Left<A, B>(override val left: A) : Either<A, B>() {
    override val right: B? = null
    override val isRight: Boolean = false
    override val flip: Either<B, A> by lazy { Right<B, A>(left) }
}

private class Right<A, B>(override val right: B) : Either<A, B>() {
    override val left: A? = null
    override val isRight: Boolean = true
    override val flip: Either<B, A> by lazy { Left<B, A>(right) }
}
