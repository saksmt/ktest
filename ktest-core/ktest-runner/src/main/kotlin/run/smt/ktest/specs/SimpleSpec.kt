package run.smt.ktest.specs

import org.junit.runner.RunWith
import run.smt.ktest.BaseSpec
import run.smt.ktest.internal.KTestJUnitRunner
import run.smt.ktest.internal.api.SpecBuilder

@RunWith(KTestJUnitRunner::class) // required to let IntelliJ discover tests
abstract class SimpleSpec(body: SimpleSpec.() -> Unit = {}) : BaseSpec() {

    init {
        body()
    }

    fun suite(name: String, body: () -> Unit) = SpecBuilder.suite(name, body)
    fun test(name: String, vararg annotations: Annotation = emptyArray(), body: () -> Unit) = SpecBuilder.case(name, annotations.toList(), body)
}
