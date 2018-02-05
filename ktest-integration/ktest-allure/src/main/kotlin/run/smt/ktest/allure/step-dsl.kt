package run.smt.ktest.allure

import io.qameta.allure.model.Parameter
import io.qameta.allure.model.Status
import io.qameta.allure.model.StepResult
import io.qameta.allure.util.ResultsUtils
import run.smt.ktest.json.dump
import run.smt.ktest.util.lambda.DualArgumentLambda
import run.smt.ktest.util.lambda.overloaded
import java.util.*

fun <R> step(name: String, description: String? = null, vararg parameters: Pair<String, Any?> = emptyArray(), body: () -> R): R {
    val cps = step(name, description, *parameters)

    try {
        return body().also {
            cps()
        }
    } catch (e: Throwable) {
        cps(e)
        throw e
    }
}

/**
 * Almost internal function
 * CPS-like usage:
 * call -> save response -> call response when finished
 */
fun step(name: String, description: String? = null, vararg parameters: Pair<String, Any?> = emptyArray()): DualArgumentLambda<Throwable, String, Unit> {
    val stepUUID = UUID.randomUUID().toString()
    val result = StepResult().withName(name).withParameters(
        parameters.map { Parameter().withName(it.first).withValue(it.second.dump()) }
    )
    allure.startStep(stepUUID, description?.let { result.withDescription(it) } ?: result)

    return { e: Throwable?, newName: String? ->
        if (e == null) {
            allure.updateStep { it
                .withStatus(ResultsUtils.getStatus(e).orElse(Status.BROKEN))
                .withStatusDetails(ResultsUtils.getStatusDetails(e).orElse(null))
                .let { if (newName == null) it else it.withName(newName) }
            }
        } else {
            allure.updateStep { it.withStatus(Status.PASSED).let { if (newName == null) it else it.withName(newName) } }
        }
        allure.stopStep(stepUUID)
    }.overloaded()
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
