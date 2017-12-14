package run.smt.ktest.specs

import org.junit.runner.RunWith
import run.smt.ktest.BaseSpec
import run.smt.ktest.internal.KTestJUnitRunner
import run.smt.ktest.internal.api.SpecBuilder

@RunWith(KTestJUnitRunner::class) // required to let IntelliJ discover tests
abstract class FeatureSpec(body: FeatureSpec.() -> Unit = {}) : BaseSpec() {

    init {
        body()
    }

    fun feature(name: String, body: () -> Unit) =
        SpecBuilder.suite("Feature: $name", body)

    fun scenario(name: String, vararg annotations: Annotation = emptyArray(), body: () -> Unit) =
        scenario(name, annotations.toList(), body)

    fun scenario(name: String, annotations: List<Annotation> = emptyList(), body: () -> Unit) =
        SpecBuilder.case("Scenario: $name", annotations, body)

}
