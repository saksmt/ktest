package run.smt.ktest.specs

import run.smt.ktest.api.BaseSpec
import run.smt.ktest.api.MetaInfoDSL
import run.smt.ktest.api.internal.SpecBuilder

abstract class FreeSpec(body: FreeSpec.() -> Unit = {}) : BaseSpec() {
    init {
        body()
    }

    inner class FreeSpecBuilder internal constructor(private val specName: String, private val metaInfo: MetaInfoDSL) {
        infix operator fun minus(body: () -> Unit) = SpecBuilder.suite(specName, metaInfo, body)
        operator fun invoke(body: () -> Unit) =
            SpecBuilder.case(specName, metaInfo, body)
    }

    infix operator fun String.invoke(metaInfo: MetaInfoDSL) = FreeSpecBuilder(this, metaInfo)

    operator fun String.minus(body: () -> Unit) = SpecBuilder.suite(this, body)

    operator fun String.invoke(metaInfo: MetaInfoDSL, body: () -> Unit) =
        SpecBuilder.case(this, metaInfo, body)
}
