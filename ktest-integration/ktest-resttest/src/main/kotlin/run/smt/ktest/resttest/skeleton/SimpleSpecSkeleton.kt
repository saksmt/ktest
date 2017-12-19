package run.smt.ktest.resttest.skeleton

import run.smt.ktest.resttest.api.RestTestSpecSkeleton
import run.smt.ktest.resttest.api.TestSpecProvider
import run.smt.ktest.specs.SimpleSpec

class SimpleSpecSkeleton : RestTestSpecSkeleton<SimpleSpec> {
    override fun SimpleSpec.execRestTest(restTestTemplate: TestSpecProvider) {
        restTestTemplate { annotations, name, body ->
            test(name, annotations, body)
        }
    }
}
