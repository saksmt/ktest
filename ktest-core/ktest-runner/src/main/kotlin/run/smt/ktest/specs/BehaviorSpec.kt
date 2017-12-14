package run.smt.ktest.specs

import org.junit.runner.RunWith
import run.smt.ktest.BaseSpec
import run.smt.ktest.internal.KTestJUnitRunner
import run.smt.ktest.internal.api.SpecBuilder

@RunWith(KTestJUnitRunner::class) // required to let IntelliJ discover tests
abstract class BehaviorSpec(body: BehaviorSpec.() -> Unit = {}) : BaseSpec() {
    init {
        body()
    }

    fun <T> given(name: String, body: () -> T) = SpecBuilder.suite("Given: $name", body)
    fun <T> `when`(name: String, body: () -> T) = SpecBuilder.suite("When: $name", body)
    fun <T> When(name: String, body: () -> T) = `when`(name, body)
    fun then(name: String, vararg annotations: Annotation = emptyArray(), body: () -> Unit) = then(name, annotations.toList(), body)
    fun then(name: String, annotations: List<Annotation> = emptyList(), body: () -> Unit) = SpecBuilder.case(name, annotations, body)
}

