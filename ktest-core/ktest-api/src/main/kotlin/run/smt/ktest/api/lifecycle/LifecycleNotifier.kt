package run.smt.ktest.api.lifecycle

import run.smt.ktest.api.Case

class LifecycleNotifier internal constructor(private val listeners: List<CaseLifecycleListener>) {
    private fun allListeners(action: CaseLifecycleListener.() -> Unit) {
        listeners.forEach(action)
    }

    fun emitCaseStart(case: Case) = allListeners { onStart(case) }
    fun emitCaseSuccess(case: Case) = allListeners { onSuccess(case) }
    fun emitCaseFailure(e: Throwable, case: Case) = allListeners { onFailure(e, case) }
    fun emitCaseFinished(case: Case) = allListeners { onFinish(case) }
    fun emitCaseSkipped(e: Throwable, case: Case) = allListeners { onSkip(e, case) }
    fun emitCaseIgnored(case: Case) = allListeners { onIgnore(case) }

    fun emitCaseRunStart(case: Case, runNumber: Int) = allListeners { onRunStart(case, runNumber) }
    fun emitCaseRunSuccess(case: Case, runNumber: Int) = allListeners { onRunSuccess(case, runNumber) }
    fun emitCaseRunFailure(e: Throwable, case: Case, runNumber: Int) = allListeners { onRunFailure(e, case, runNumber) }
    fun emitCaseRunFinished(case: Case, runNumber: Int) = allListeners { onRunFinish(case, runNumber) }
    fun emitCaseRunSkipped(e: Throwable, case: Case, runNumber: Int) = allListeners { onRunSkip(e, case, runNumber) }
}
