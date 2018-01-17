package run.smt.ktest.specs

import run.smt.ktest.api.BaseSpec
import run.smt.ktest.api.MetaInfoDSL
import run.smt.ktest.api.internal.SpecBuilder

abstract class WordSpec(body: WordSpec.() -> Unit = {}) : BaseSpec() {
    init {
        body()
    }

    infix fun String.should(body: () -> Unit) = SpecBuilder.suite("${this@should} should", body)
    operator fun String.invoke(body: () -> Unit) = SpecBuilder.case(this, body)
    operator fun String.invoke(metaInfo: MetaInfoDSL, body: () -> Unit) = SpecBuilder.case(this, metaInfo, body)
}
