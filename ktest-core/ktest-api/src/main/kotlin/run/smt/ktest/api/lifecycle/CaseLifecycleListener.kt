package run.smt.ktest.api.lifecycle

import run.smt.ktest.api.Case

interface CaseLifecycleListener {
    fun onStart(case: Case) {}
    fun onSuccess(case: Case) {}
    fun onFailure(exception: Throwable, case: Case) {}
    fun onFinish(case: Case) {}
    fun onSkip(cause: Throwable, case: Case) {}
    fun onIgnore(case: Case) {}

    fun onRunStart(case: Case, runNumber: Int) {}
    fun onRunSuccess(case: Case, runNumber: Int) {}
    fun onRunFailure(exception: Throwable, case: Case, runNumber: Int) {}
    fun onRunFinish(case: Case, runNumber: Int) {}
    fun onRunSkip(exception: Throwable, case: Case, runNumber: Int) {}
}
