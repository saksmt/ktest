package run.smt.ktest.util.collection

fun <T> Iterable<T>.head() = this.first()
fun <T> Iterable<T>.headOption() = this.firstOrNull()
fun <T> Iterable<T>.tail() = this.drop(1)

fun <T> Iterable<T>.init() = this.reversed().tail().reversed()

fun <T> Sequence<T>.head() = this.first()
fun <T> Sequence<T>.headOption() = this.firstOrNull()
fun <T> Sequence<T>.tail() = this.drop(1)


fun <T, R> Iterable<T>.scan(start: R, accFunc: (R, T) -> R): List<R> = fold(listOf(start)) { acc, newItem ->
    acc + (accFunc(acc.last(), newItem))
}

fun <T, R> Sequence<T>.scan(start: R, accFunc: (R, T) -> R): Sequence<R> = fold(sequenceOf(start)) { acc, newItem ->
    acc + (accFunc(acc.last(), newItem))
}

fun <T> Collection<T>.padTo(newSize: Int): Collection<T?> {
    return padTo(newSize, null)
}

fun <T> Collection<T>.padTo(newSize: Int, e: T): Collection<T> {
    return if (size < newSize) {
        this + (0..(newSize - size)).map { e }
    } else {
        this
    }
}

fun <T> Sequence<T>.zipWithIndex(): Sequence<Pair<Int, T>> = mapIndexed { i, v -> i to v }
fun <T> Iterable<T>.zipWithIndex(): Iterable<Pair<Int, T>> = mapIndexed { i, v -> i to v }

fun <T> Sequence<T>.permutations(): Sequence<Sequence<T>> {
    headOption() ?: return emptySequence()

    val tail = tail()
    tail.headOption() ?: return sequenceOf(this)
    return fold(emptySequence<Sequence<T>>()) { xs, x ->
        xs + filterNot { it == x }.permutations().map { sequenceOf(x) + it }.asSequence()
    }
}

fun <L, R> Sequence<L>.crossProduct(that: Sequence<R>): Sequence<Pair<L, R>> {
    return flatMap { l ->
        that.map { r ->
            l to r
        }
    }
}
