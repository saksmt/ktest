package run.smt.ktest.allure.listener

import io.qameta.allure.model.Label
import io.qameta.allure.model.Status
import io.qameta.allure.model.StatusDetails
import io.qameta.allure.model.TestResult
import io.qameta.allure.util.ResultsUtils
import run.smt.ktest.allure.allure
import run.smt.ktest.allure.model.*
import run.smt.ktest.api.Case
import run.smt.ktest.api.CategoryProperty
import run.smt.ktest.api.MetaData
import run.smt.ktest.api.Suite
import run.smt.ktest.api.lifecycle.CaseLifecycleListener
import java.math.BigInteger
import java.security.MessageDigest

class AllureCaseListener : CaseLifecycleListener {
    override fun onStart(case: Case) {
        val testResult = case.toResult()
        allure.scheduleTestCase(testResult)
        allure.startTestCase(case.uid)
    }

    override fun onFailure(exception: Throwable, case: Case) {
        allure.updateTestCase(case.uid) {
            it.status = ResultsUtils.getStatus(exception).orElse(null)
            it.statusDetails = ResultsUtils.getStatusDetails(exception).orElse(null)?.let {
                parseDetailsFlags(it, case)
            }
        }
    }

    override fun onFinish(case: Case) {
        allure.updateTestCase(case.uid) {
            it.status ?: it.run {
                status = Status.PASSED
            }
        }

        allure.stopTestCase(case.uid)
        allure.writeTestCase(case.uid)
    }

    override fun onSkip(cause: Throwable, case: Case) {
        allure.updateTestCase(case.uid) {
            it.status = Status.SKIPPED
            it.statusDetails = ResultsUtils.getStatusDetails(cause).orElse(null)?.let {
                parseDetailsFlags(it, case)
            }
        }
    }

    override fun onIgnore(case: Case) {
        val result = case.toResult()

        result.status = Status.SKIPPED
        result.statusDetails = StatusDetails().apply {
            message = case.disablingReason ?: "Test ignored (without reason)!"
        }
        result.start = System.currentTimeMillis()

        allure.scheduleTestCase(result)
        allure.stopTestCase(case.uid)
        allure.writeTestCase(case.uid)
    }

    private fun parseDetailsFlags(details: StatusDetails, case: Case): StatusDetails {
        return details.apply {
            isFlaky = case.inheritedMetadata.any { it is Flaky }
            isMuted = case.inheritedMetadata.any { it is Muted }
        }
    }

    private fun Case.toResult(): TestResult {
        val case = this
        val testClass = case.suite.testClass
        val userLabels = case.inheritedMetadata.asSequence()
            .filterIsInstance<AllureLabelProperty>()
            .map { it.value }
            .toList()

        val userLinks = case.inheritedMetadata.asSequence()
            .filterIsInstance<AllureLinkProperty>()
            .map { it.value }
            .toList()

        val displayName = case.metaData.allureDisplayName()

        val userDescription = case.metaData
            .filterIsInstance<Description>()
            .map { it.value }
            .firstOrNull()

        return TestResult().apply {
            uuid = case.uid

            historyId = BigInteger(1, MessageDigest.getInstance("md5").digest(
                (testClass.qualifiedName + case.name + case.suite.fullName).toByteArray()
            )).toString(16)

            labels = userLabels + listOf(
                testClass.java.`package`.name named "package",
                testClass.qualifiedName named "testClass",
                case.suite.fullName named "suite",
                ResultsUtils.createHostLabel(),
                ResultsUtils.createThreadLabel()
            ) + collectCategories(case)

            links = userLinks

            fullName = "${case.suite.allureFullName}.${case.name}"

            name = displayName ?: case.name
            description = userDescription
        }
    }

    private fun collectCategories(case: Case): List<Label> {
        return case.inheritedMetadata.asSequence()
            .filterIsInstance<CategoryProperty>()
            .map { it.value named "tag" }
            .toList()
    }

    private val Suite.allureFullName
        get() = suiteFullName(this, "").removePrefix(".")

    private fun MetaData.allureDisplayName() = this
            .filterIsInstance<DisplayName>()
            .map { it.value }
            .firstOrNull()

    private tailrec fun suiteFullName(parentSuite: Suite, thisName: String): String {
        val parentLevel = parentSuite.metaData.allureDisplayName() ?: parentSuite.name

        val currentFullName = "$parentLevel.$thisName"

        return if (parentSuite.parent.isRight) {
            currentFullName
        } else {
            suiteFullName(parentSuite.parent.left!!, currentFullName)
        }
    }

    private infix fun String?.named(name: String) = Label().apply {
        this.name = name
        value = this@named
    }
}
