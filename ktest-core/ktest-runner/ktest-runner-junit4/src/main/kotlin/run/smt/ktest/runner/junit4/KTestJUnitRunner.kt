package run.smt.ktest.runner.junit4

import org.junit.experimental.categories.Category
import org.junit.runner.Description
import org.junit.runner.notification.RunNotifier
import org.junit.runners.ParentRunner
import run.smt.ktest.api.*
import run.smt.ktest.api.internal.ExecutableCase
import run.smt.ktest.api.internal.InitializationMode
import run.smt.ktest.api.internal.SpecExecutor
import run.smt.ktest.util.reflection.a

internal class JUnit4RunnerDescription : RunnerDescription {
    override val name: String
        get() = "JUnit"
    override val version: String
        get() = "4"

    var notifier: RunNotifier? = null
}

class KTestJUnitRunner(testClass: Class<BaseSpec>) : ParentRunner<ExecutableCase>(testClass) {
    private val runnerDescription = JUnit4RunnerDescription()
    private val executor = SpecExecutor(testClass, InitializationMode.EAGER, runnerDescription)

    override fun getName() = executor.rootSuite.name

    override fun getDescription() = describe(executor.rootSuite)

    override fun describeChild(child: ExecutableCase): Description = describe(child.case)

    override fun run(notifier: RunNotifier?) {
        notifier ?: return

        runnerDescription.notifier = notifier
        executor.startup()

        super.run(notifier)

        executor.finalize()
    }

    override fun runChild(case: ExecutableCase, notifier: RunNotifier) {
        case.execute()
    }

    override fun getChildren() = executor.executables
}

internal fun describe(suite: Suite): Description {
    val description = Description.createSuiteDescription(sanitizeSpecName(suite.name), suite.inheritedMetaData.collectAnnotations())
    suite.childSuites.forEach {
        description.addChild(describe(it))
    }
    suite.childCases.forEach {
        description.addChild(describe(it))
    }
    return description
}

internal fun describe(case: Case) = Description.createTestDescription(
    sanitizeSpecName(case.suite.name),
    sanitizeSpecName(case.name).let { if (case.invocations < 2) it else it + " (${case.invocations} invocations)" },
    case.inheritedMetadata.collectAnnotations()
)

internal fun MetaData.collectAnnotations() = (asSequence()
    .filterIsInstance<AnnotationBasedProperty<*>>()
    .map { it.value }
    .toSet() + collectCategories())
    .toTypedArray()

internal fun MetaData.collectCategories(): Set<Annotation> {
    val categories = asSequence()
        .filterIsInstance<CategoryProperty>()
        .map { it.value }
        .mapNotNull { try { Class.forName(it) } catch (e: Exception) { null } }
        .toSet()

    return if (categories.isEmpty()) { emptySet() } else {
        setOf(a<Category>(categories.toTypedArray()))
    }
}

internal fun sanitizeSpecName(name: String) = name.replace("(", " ").replace(")", " ")
