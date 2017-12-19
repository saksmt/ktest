package run.smt.ktest.resttest.skeleton

import run.smt.ktest.resttest.api.RestTestSpecSkeleton
import run.smt.ktest.resttest.api.TestSpecProvider
import run.smt.ktest.specs.FreeSpec

class FreeSpecSkeleton : RestTestSpecSkeleton<FreeSpec> {
    override fun FreeSpec.execRestTest(restTestTemplate: TestSpecProvider) {
        restTestTemplate { annotations, name, body ->
            name(annotations, body)
        }
    }
}
