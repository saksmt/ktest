package run.smt.ktest.resttest.skeleton

import run.smt.ktest.resttest.api.RestTestSpecSkeleton
import run.smt.ktest.resttest.api.TestSpecProvider
import run.smt.ktest.specs.BehaviorSpec

class BehaviorSpecSkeleton : RestTestSpecSkeleton<BehaviorSpec> {
    override fun BehaviorSpec.execRestTest(restTestTemplate: TestSpecProvider) {
        restTestTemplate { annotations, name, body ->
            then(name, annotations, body)
        }
    }
}
