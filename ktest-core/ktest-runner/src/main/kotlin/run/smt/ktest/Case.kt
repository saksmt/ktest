package run.smt.ktest

import org.junit.runner.Description
import run.smt.ktest.internal.api.Suite
import run.smt.ktest.internal.api.Tag
import java.time.Duration

data class Case(
    private val suite: Suite,
    val name: String,
    val test: () -> Unit,
    var config: TestCaseConfig,
    val annotations: List<Annotation> = emptyList()) {

    internal val description: Description
        get() = Description.createTestDescription(
            suite.name,
            (if (config.invocations < 2) name else name + " (${config.invocations} invocations)"),
            *(annotations + config.annotations + suite.parents.flatMap { it.annotations }).toSet().toTypedArray()
        )

    fun config(
        invocations: Int? = null,
        enabled: Boolean? = null,
        disablingReason: String? = null,
        timeout: Duration? = null,
        threads: Int? = null,
        tags: Set<Tag>? = null,
        annotations: List<Annotation>? = null) {
        config =
            TestCaseConfig(
                enabled ?: config.enabled,
                disablingReason ?: config.disablingReason,
                invocations ?: config.invocations,
                timeout ?: config.timeout,
                threads ?: config.threads,
                tags ?: config.tags,
                annotations ?: emptyList())
    }

    internal val isActive: Boolean
        get() = config.enabled && isActiveAccordingToTags

    private val isActiveAccordingToTags: Boolean
        get() {
            val testCaseTags = config.tags.map { it.toString() }
            val includedTags = readProperty("includeTags")
            val excludedTags = readProperty("excludeTags")
            val includedTagsEmpty = includedTags.isEmpty() || includedTags == listOf("")
            return when {
                excludedTags.intersect(testCaseTags).isNotEmpty() -> false
                includedTagsEmpty -> true
                includedTags.intersect(testCaseTags).isNotEmpty() -> true
                else -> false
            }
        }

    private fun readProperty(name: String): List<String> =
        (System.getProperty(name) ?: "").split(',').map { it.trim() }

    // required to avoid StackOverflowError due to mutable data structures in this class (suite)
    override fun toString(): String = name
}
