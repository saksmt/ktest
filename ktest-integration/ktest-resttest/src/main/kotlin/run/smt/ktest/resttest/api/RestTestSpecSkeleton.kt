package run.smt.ktest.resttest.api

import run.smt.ktest.api.BaseSpec
import run.smt.ktest.api.MetaInfoDSL

typealias TestSpec = (metaInfoBuilder: MetaInfoDSL, name: String, body: () -> Unit) -> Unit
typealias TestSpecProvider = (TestSpec) -> Unit

interface RestTestSpecSkeleton<in S : BaseSpec> {
    operator fun invoke(spec: S, restTestTemplate: TestSpecProvider) {
        with(spec) {
            execRestTest(restTestTemplate)
        }
    }

    fun S.execRestTest(restTestTemplate: TestSpecProvider)
}
