package run.smt.ktest.resttest.api

import run.smt.ktest.BaseSpec

typealias TestSpec = (annotations: List<Annotation>, name: String, body: () -> Unit) -> Unit
typealias TestSpecProvider = (TestSpec) -> Unit

interface RestTestSpecSkeleton<in S : BaseSpec> {
    operator fun invoke(spec: S, restTestTemplate: TestSpecProvider) {
        with(spec) {
            execRestTest(restTestTemplate)
        }
    }

    fun S.execRestTest(restTestTemplate: TestSpecProvider)
}
