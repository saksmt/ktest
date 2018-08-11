package run.smt.ktest.api

import run.smt.ktest.config.config
import run.smt.ktest.config.get
import java.time.Duration
import java.util.*

/**
 * Represents case to be executed
 */
data class Case(
    /**
     * Suite where this case belongs to
     */
    val suite: Suite,
    val name: String,
    /**
     * Metadata for this case
     */
    val metaData: MetaData,
    val body: () -> Unit
) {
    val uid = UUID.randomUUID().toString()

    /**
     * Case metadata + metadata of its parent suite (and all its parents)
     */
    val inheritedMetadata = metaData + suite.inheritedMetaData

    private val disabled = metaData.filterIsInstance<Disabled>().firstOrNull()

    /**
     * Reason why this case was disabled
     */
    val disablingReason = disabled?.value

    /**
     * Is this case enabled?
     */
    val enabled = disabled == null

    /**
     * How much times this case should be executed?
     */
    val invocations = metaData.filterIsInstance<MultipleInvocationsProperty>().firstOrNull()?.value ?: 1

    /**
     * Timeout for this case (or it's single invocation if there is more than one invocation)
     */
    val timeout = metaData.filterIsInstance<TimeoutProperty>().firstOrNull()?.value ?: if (config.hasPath("test.timeout")) {
        config["test.timeout"]
    } else {
        Duration.ofNanos(Long.MAX_VALUE)!!
    }

    /**
     * How many threads should be used to execute this case (only makes sense if this case has more than one invocation)
     */
    val threads = metaData.filterIsInstance<ThreadsProperty>().firstOrNull()?.value ?: 1
}
