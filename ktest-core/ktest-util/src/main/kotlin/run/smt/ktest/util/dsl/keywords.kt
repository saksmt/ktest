package run.smt.ktest.util.dsl

fun <T> using(v: T) = v
fun <T, R> using(dsl: (T.() -> R) -> R, dslApplication: T.() -> R) = dsl(dslApplication)
infix fun <T, R> T.execute(dsl: T.() -> R) = dsl()
