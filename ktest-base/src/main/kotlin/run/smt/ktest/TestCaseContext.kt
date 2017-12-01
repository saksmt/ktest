package run.smt.ktest

/**
 * Meta information about a [TestCase].
 */
data class TestCaseContext(
    val spec: Spec,
    val testCase: TestCase)