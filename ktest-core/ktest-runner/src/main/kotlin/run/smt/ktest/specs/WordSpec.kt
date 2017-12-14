package run.smt.ktest.specs

import org.junit.runner.RunWith
import run.smt.ktest.BaseSpec
import run.smt.ktest.internal.KTestJUnitRunner
import run.smt.ktest.internal.api.SpecBuilder

@RunWith(KTestJUnitRunner::class) // required to let IntelliJ discover tests
abstract class WordSpec(body: WordSpec.() -> Unit = {}) : BaseSpec() {
    init {
        body()
    }

    infix fun String.should(body: () -> Unit) = SpecBuilder.suite("${this@should} should", body)
    operator fun String.invoke(vararg annotations: Annotation, body: () -> Unit) = this(annotations.toList(), body)
    operator fun String.invoke(annotations: List<Annotation>, body: () -> Unit) = SpecBuilder.case(this, annotations, body)
}
