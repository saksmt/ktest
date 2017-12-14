package run.smt.ktest.allure

import io.qameta.allure.model.Parameter
import io.qameta.allure.model.Status
import io.qameta.allure.model.StepResult
import io.qameta.allure.util.ResultsUtils
import run.smt.ktest.json.dump
import java.util.*

fun <R> step(name: String, description: String? = null, vararg parameters: Pair<String, Any?> = emptyArray(), body: () -> R): R {
    val stepUUID = UUID.randomUUID().toString()
    val result = StepResult().withName(name).withParameters(
        parameters.map { Parameter().withName(it.first).withValue(it.second.dump()) }
    )
    allure.startStep(stepUUID, description?.let { result.withDescription(it) } ?: result)
    try {
        return body().also {
            allure.updateStep { it.withStatus(Status.PASSED) }
        }
    } catch (e: Throwable) {
        allure.updateStep { it
            .withStatus(ResultsUtils.getStatus(e).orElse(Status.BROKEN))
            .withStatusDetails(ResultsUtils.getStatusDetails(e).orElse(null))
        }
        throw e
    } finally {
        allure.stopStep(stepUUID)
    }
}

/**
 * Marks block of code as allure step
 */
infix fun <R> String.step(body: () -> R): R = step(name = this, body = body)

/**
 * Marks block of code as allure step
 */
infix fun <R> (() -> R).named(name: String): R = name.step(this)

/**
 * Marks block of code as allure step
 */
infix fun <R> (() -> R).namely(name: String): R = name.step(this)
