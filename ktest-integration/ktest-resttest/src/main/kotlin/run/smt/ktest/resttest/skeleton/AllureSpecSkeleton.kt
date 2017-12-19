package run.smt.ktest.resttest.skeleton

import run.smt.ktest.resttest.api.RestTestSpecSkeleton
import run.smt.ktest.resttest.api.TestSpecProvider
import run.smt.ktest.specs.AllureSpec

class AllureSpecSkeleton : RestTestSpecSkeleton<AllureSpec> {
    override fun AllureSpec.execRestTest(restTestTemplate: TestSpecProvider) {
        restTestTemplate { annotations, name, body ->
            case(name, annotations, body)
        }
    }
}
