package run.smt.ktest.resttest.skeleton

import run.smt.ktest.resttest.api.RestTestSpecSkeleton
import run.smt.ktest.resttest.api.TestSpecProvider
import run.smt.ktest.specs.FeatureSpec

class FeatureSpecSkeleton : RestTestSpecSkeleton<FeatureSpec> {
    override fun FeatureSpec.execRestTest(restTestTemplate: TestSpecProvider) {
        restTestTemplate { annotations, name, body ->
            scenario(name, annotations, body)
        }
    }
}
