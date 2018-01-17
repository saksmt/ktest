package run.smt.ktest.specs

import run.smt.ktest.api.BaseSpec
import run.smt.ktest.api.MetaInfoDSL
import run.smt.ktest.api.internal.SpecBuilder

abstract class BehaviorSpec(body: BehaviorSpec.() -> Unit = {}) : BaseSpec() {
    init {
        body()
    }

    fun <T> given(name: String, body: () -> T) = SpecBuilder.suite("Given: $name", body)
    fun <T> given(name: String, metaInfo: MetaInfoDSL, body: () -> T) = SpecBuilder.suite("Given: $name", metaInfo, body)
    fun <T> `when`(name: String, body: () -> T) = SpecBuilder.suite("When: $name", body)
    fun <T> `when`(name: String, metaInfo: MetaInfoDSL, body: () -> T) = SpecBuilder.suite("When: $name", metaInfo, body)
    fun <T> When(name: String, body: () -> T) = `when`(name, body)
    fun <T> When(name: String, metaInfo: MetaInfoDSL, body: () -> T) = `when`(name, metaInfo, body)
    fun then(name: String, body: () -> Unit) = SpecBuilder.case("Then: $name", body)
    fun then(name: String, metaInfo: MetaInfoDSL, body: () -> Unit) = SpecBuilder.case("Then: $name", metaInfo, body)
}
