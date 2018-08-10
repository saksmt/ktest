package run.smt.ktest.api

import run.smt.ktest.config.config
import run.smt.ktest.config.get
import java.time.Duration
import java.util.*

data class Case(
    val suite: Suite,
    val name: String,
    val metaData: MetaData,
    val body: () -> Unit
) {
    val uid = UUID.randomUUID().toString()

    val inheritedMetadata = metaData + suite.inheritedMetaData

    private val disabled = metaData.filterIsInstance<Disabled>().firstOrNull()

    val disablingReason = disabled?.value
    val enabled = disabled == null

    val invocations = metaData.filterIsInstance<MultipleInvocationsProperty>().firstOrNull()?.value ?: 1

    val timeout = metaData.filterIsInstance<TimeoutProperty>().firstOrNull()?.value ?: if (config.hasPath("test.timeout")) {
        config["test.timeout"]
    } else {
        Duration.ofNanos(Long.MAX_VALUE)!!
    }
    val threads = metaData.filterIsInstance<ThreadsProperty>().firstOrNull()?.value ?: 1
}
