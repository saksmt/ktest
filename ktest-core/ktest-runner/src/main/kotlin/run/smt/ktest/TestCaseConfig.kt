package run.smt.ktest

import run.smt.ktest.internal.api.Tag
import run.smt.ktest.util.duration.infiniteDuration
import java.time.Duration

data class TestCaseConfig(
    val enabled: Boolean = true,
    val disablingReason: String? = null,
    val invocations: Int = 1,
    val timeout: Duration = infiniteDuration,
    val threads: Int = 1,
    val tags: Set<Tag> = setOf(),
    val annotations: List<Annotation> = emptyList()
)
