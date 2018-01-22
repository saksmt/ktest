package run.smt.ktest.specs

import run.smt.ktest.api.BaseSpec
import run.smt.ktest.api.MetaInfoDSL
import run.smt.ktest.api.internal.SpecBuilder

abstract class SimpleSpec(body: SimpleSpec.() -> Unit = {}) : BaseSpec() {

    init {
        body()
    }

    fun suite(name: String, body: () -> Unit) = SpecBuilder.suite(name, body)
    fun suite(name: String, metaInfo: MetaInfoDSL, body: () -> Unit) = SpecBuilder.suite(name, metaInfo, body)
    fun test(name: String, body: () -> Unit) = SpecBuilder.case(name, body)
    fun test(name: String, metaInfo: MetaInfoDSL, body: () -> Unit) = SpecBuilder.case(name, metaInfo, body)
}
