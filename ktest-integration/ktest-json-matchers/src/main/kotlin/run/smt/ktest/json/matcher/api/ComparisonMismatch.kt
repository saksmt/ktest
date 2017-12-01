package run.smt.ktest.json.matcher.api

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.MissingNode

sealed class ComparisonMismatch(expected: JsonNode?, actual: JsonNode?, val path: ComparisonPath, messageSupplier: (ComparisonMismatch.() -> String)?) {
    val expected = expected ?: MissingNode.getInstance()!!
    val actual = actual ?: MissingNode.getInstance()!!
    val message = messageSupplier?.invoke(this) ?: throw IllegalStateException("NO MESSAGE PROVIDED!!!")

    protected fun positionMessage() = if (path.expectedPath == path.actualPath) {
        "At path ${path.expectedPath} with position in expected [${expected.pos()}] and position in actual [${actual.pos()}]"
    } else {
        "At expected (path: ${path.expectedPath}, position: [${expected.pos()}]) and actual (path: ${path.actualPath}, position: [${actual.pos()}])"
    }

    private fun JsonNode.pos(): String {
        val location = traverse()?.currentLocation ?: return "unknown"
        return "${location.sourceRef}:${location.lineNr},${location.columnNr}"
    }

    override fun toString(): String {
        return "${javaClass.simpleName}(${path.expectedPath} / ${path.actualPath}): $message"
    }

    class MissingValue(e: JsonNode?, a: JsonNode?, p: ComparisonPath) : ComparisonMismatch(e, a, p, {
        "Expected node of type ${expected.nodeType.name}, got $actual; ${positionMessage()}"
    })

    class UnexpectedValue(e: JsonNode?, a: JsonNode?, p: ComparisonPath): ComparisonMismatch(e, a, p, {
        "Expected null/missing node but got node of type: ${actual.nodeType.name}; ${positionMessage()}"
    })

    class ExpectedNull(e: JsonNode?, a: JsonNode?, p: ComparisonPath): ComparisonMismatch(e, a, p, {
        "Expected null node but got node of type: ${actual.nodeType.name}; ${positionMessage()}"
    })

    class TypeMismatch(e: JsonNode?, a: JsonNode?, p: ComparisonPath): ComparisonMismatch(e, a, p, {
        "Expected node of type ${expected.nodeType.name}, got ${actual.nodeType}; ${positionMessage()}"
    })

    class BooleanMismatch(e: JsonNode?, a: JsonNode?, p: ComparisonPath): ComparisonMismatch(e, a, p, {
        "Boolean node differs. Expected: $expected, got: $actual; ${positionMessage()}"
    })

    class NumericMismatch(e: JsonNode?, a: JsonNode?, p: ComparisonPath): ComparisonMismatch(e, a, p, {
        "Numeric node differs. Expected: $expected, got: $actual; ${positionMessage()}"
    })

    class ValueMismatch(e: JsonNode?, a: JsonNode?, p: ComparisonPath): ComparisonMismatch(e, a, p, {
        "Node value differs. Expected: \"$expected\", got: \"$actual\"; ${positionMessage()}"
    })

    class StructuralMismatch(val extraFields: List<String>, val missingFields: List<String>, e: JsonNode, a: JsonNode?, p: ComparisonPath): ComparisonMismatch(e, a, p, {
        "Structure of nodes differs. " +
            (if (extraFields.isNotEmpty()) "Extra fields found: \"${extraFields.joinToString("\", \"")}\". " else "") +
            (if (missingFields.isNotEmpty()) "Missing fields: \"${missingFields.joinToString("\", \"")}\". " else "") +
            positionMessage()
    })

    class ArraySizeMismatch(val expectedSize: Int, val actualSize: Int, e: JsonNode?, a: JsonNode?, p: ComparisonPath): ComparisonMismatch(e, a, p, {
        "Array sizes are different, expected $expectedSize elements, got $actualSize elements; ${positionMessage()}"
    })
}
