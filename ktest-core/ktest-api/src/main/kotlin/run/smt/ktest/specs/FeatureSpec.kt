package run.smt.ktest.specs

import run.smt.ktest.api.BaseSpec
import run.smt.ktest.api.MetaInfoDSL
import run.smt.ktest.api.internal.SpecBuilder

abstract class FeatureSpec(body: FeatureSpec.() -> Unit = {}) : BaseSpec() {

    init {
        body()
    }

    fun feature(name: String, body: () -> Unit) =
        SpecBuilder.suite("Feature: $name", body)

    fun feature(name: String, metaInfo: MetaInfoDSL, body: () -> Unit) =
        SpecBuilder.suite("Feature: $name", metaInfo, body)


    fun scenario(name: String, body: () -> Unit) =
        SpecBuilder.case("Scenario: $name", body)

    fun scenario(name: String, metaInfo: MetaInfoDSL, body: () -> Unit) =
        SpecBuilder.case("Scenario: $name", metaInfo, body)

}
