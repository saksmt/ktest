package run.smt.ktest.resttest.skeleton

import run.smt.ktest.resttest.api.RestTestSpecSkeleton
import run.smt.ktest.resttest.api.TestSpecProvider
import run.smt.ktest.specs.WordSpec

class WordSpecSkeleton : RestTestSpecSkeleton<WordSpec> {
    override fun WordSpec.execRestTest(restTestTemplate: TestSpecProvider) {
        restTestTemplate { annotations, name, body ->
            name(annotations, body)
        }
    }
}
