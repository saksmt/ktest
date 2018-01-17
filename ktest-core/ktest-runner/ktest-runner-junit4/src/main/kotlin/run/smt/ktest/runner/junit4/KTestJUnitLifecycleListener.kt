package run.smt.ktest.runner.junit4

import org.junit.Ignore
import org.junit.runner.notification.Failure
import run.smt.ktest.api.BaseSpec
import run.smt.ktest.api.Case
import run.smt.ktest.api.internal.Internals
import run.smt.ktest.api.lifecycle.CaseLifecycleListener
import run.smt.ktest.api.metaInfo

class KTestJUnitLifecycleListener(private val spec: BaseSpec) : CaseLifecycleListener {
    override fun onStart(case: Case) {
        spec.notifier?.fireTestStarted(describe(case))
    }

    override fun onFailure(exception: Throwable, case: Case) {
        spec.notifier?.fireTestFailure(Failure(describe(case), exception))
    }

    override fun onFinish(case: Case) {
        spec.notifier?.fireTestFinished(describe(case))
    }

    override fun onSkip(cause: Throwable, case: Case) {
        spec.notifier?.fireTestAssumptionFailed(Failure(describe(case), cause))
    }

    override fun onIgnore(case: Case) {
        spec.notifier?.fireTestIgnored(describe(
            case.copy(metaData = case.metaData + metaInfo { a<Ignore>(case.disablingReason ?: "") })
        ))
    }

    override fun onRunFailure(exception: Throwable, case: Case, runNumber: Int) {
        spec.notifier?.fireTestFailure(Failure(describe(case), exception))
    }

    private val BaseSpec.notifier
        get() = (Internals.runnerDescription as? JUnit4RunnerDescription)?.notifier
}
