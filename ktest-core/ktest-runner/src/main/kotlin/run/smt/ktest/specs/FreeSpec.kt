package run.smt.ktest.specs

import org.junit.runner.RunWith
import run.smt.ktest.BaseSpec
import run.smt.ktest.internal.KTestJUnitRunner
import run.smt.ktest.internal.api.SpecBuilder

@RunWith(KTestJUnitRunner::class) // required to let IntelliJ discover tests
abstract class FreeSpec(body: FreeSpec.() -> Unit = {}) : BaseSpec() {
    init {
        body()
    }

    operator fun String.minus(body: () -> Unit) = SpecBuilder.suite(this, body)
    operator fun String.invoke(vararg annotations: Annotation = emptyArray(), body: () -> Unit) =
        this(annotations.toList(), body)

    operator fun String.invoke(annotations: List<Annotation> = emptyList(), body: () -> Unit) =
        SpecBuilder.case(this, annotations, body)
}
