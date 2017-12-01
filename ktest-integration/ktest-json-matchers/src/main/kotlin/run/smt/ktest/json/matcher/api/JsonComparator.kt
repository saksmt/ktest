package run.smt.ktest.json.matcher.api

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.*
import run.smt.ktest.util.collection.*
import run.smt.ktest.json.matcher.api.ComparisonMismatch.*

private typealias Failure = (JsonNode?, JsonNode?, ComparisonPath) -> ComparisonMismatch

class JsonComparator internal constructor(private val arrayComparisonMode: ArrayComparisonMode, private val strictNullChecking: Boolean = false, private val strictObjectFields: Boolean = true, private val fieldDifferenceThreshold: Int = 5) {

    fun diff(expected: JsonNode, actual: JsonNode): List<ComparisonMismatch> {
        val diff = mutableListOf<ComparisonMismatch>()
        diff(expected, actual, ComparisonPath(), diff)
        return diff.toList()
    }

    private fun diff(expected: JsonNode?, actual: JsonNode?, path: ComparisonPath, failed: MutableList<ComparisonMismatch>) {
        fun fail(failure: Failure) {
            failed += failure(expected, actual, path)
        }

        fun assert(condition: Boolean, failure: Failure) {
            if (!condition) {
                fail(failure)
            }
        }

        return when {
            isNullJson(actual) || isNullJson(expected) -> compareNullNodes(actual, expected, path, failed)
            actual == null || expected == null -> { /* make kotlin happy */ }
            actual.nodeType != expected.nodeType -> fail(ComparisonMismatch::TypeMismatch)
            else -> when (actual.nodeType) {
                JsonNodeType.ARRAY -> compareJsonArrays(expected as ArrayNode, actual as ArrayNode, path, failed)
                JsonNodeType.OBJECT -> compareJsonObjects(expected as ObjectNode, actual as ObjectNode, path, failed)
                JsonNodeType.BOOLEAN -> assert(expected.booleanValue() == actual.booleanValue(), ComparisonMismatch::BooleanMismatch)
                JsonNodeType.NUMBER -> assert(expected.numberValue() == actual.numberValue(), ComparisonMismatch::NumericMismatch)
                else -> assert(expected == actual, ComparisonMismatch::ValueMismatch)
            }
        }
    }

    private fun compareNullNodes(actual: JsonNode?, expected: JsonNode?, path: ComparisonPath, failed: MutableList<ComparisonMismatch>) {
        val a = actual ?: MissingNode.getInstance()
        val e = expected ?: MissingNode.getInstance()

        fun fail(failure: (JsonNode?, JsonNode?, ComparisonPath) -> ComparisonMismatch) {
            failed += failure(expected, actual, path)
        }

        fun strictOnly(failure: (JsonNode?, JsonNode?, ComparisonPath) -> ComparisonMismatch) {
            if (strictNullChecking && a.nodeType != e.nodeType) {
                fail(failure)
            }
        }

        return when {
            e.nodeType == a.nodeType -> {}
            e.nodeType == JsonNodeType.NULL ->
                when (a.nodeType) {
                    JsonNodeType.MISSING -> strictOnly(ComparisonMismatch::ExpectedNull)
                    else -> fail(ComparisonMismatch::ExpectedNull)
                }
            e.nodeType == JsonNodeType.MISSING ->
                when (a.nodeType) {
                    JsonNodeType.NULL -> strictOnly(ComparisonMismatch::UnexpectedValue)
                    else -> fail(ComparisonMismatch::UnexpectedValue)
                }
            else -> fail(ComparisonMismatch::MissingValue)
        }
    }

    private fun isNullJson(v: JsonNode?): Boolean {
        return v == null || v is NullNode || v is MissingNode
    }

    private fun compareJsonObjects(expected: ObjectNode, actual: ObjectNode, path: ComparisonPath, failed: MutableList<ComparisonMismatch>) {

        val expectedFieldNames = expected.fieldNames().asSequence().sorted().toMutableList()
        val actualFieldNames = actual.fieldNames().asSequence().sorted().toMutableList()

        if (expectedFieldNames != actualFieldNames) {
            val extraFields = actualFieldNames - expectedFieldNames
            val missingFields = expectedFieldNames - actualFieldNames
            if ((extraFields.isNotEmpty() && strictObjectFields) || (missingFields.isNotEmpty() && strictNullChecking)) {
                failed += StructuralMismatch(extraFields, missingFields, expected, actual, path)
            }

            if (strictObjectFields && extraFields.size > fieldDifferenceThreshold) {
                return
            }

            if (strictNullChecking && missingFields.size > fieldDifferenceThreshold) {
                return
            }

            if (strictNullChecking && strictObjectFields && expectedFieldNames.size + missingFields.size > fieldDifferenceThreshold) {
                return
            }
            extraFields.forEach {
                actualFieldNames.remove(it)
            }
            missingFields.forEach {
                expectedFieldNames.remove(it)
            }
        }

        actualFieldNames.asSequence()
            .sorted()
            .zip(expectedFieldNames.asSequence().sorted())
            .forEach { (actualFieldName, expectedFieldName) ->
                diff(expected.get(expectedFieldName), actual.get(actualFieldName), path + actualFieldName, failed)
            }
    }

    private fun <T> sort(toBeSorted: Iterable<Pair<Int, T>>): Iterable<Pair<Int, T>> {
        if (arrayComparisonMode == ArrayComparisonMode.STRICT) {
            return toBeSorted
        }
        return toBeSorted.asSequence().sortedBy { it.second.toString() }.asIterable()
    }

    private fun compareJsonArrays(expected: ArrayNode, actual: ArrayNode, path: ComparisonPath, failed: MutableList<ComparisonMismatch>) {
        if (actual.size() != expected.size()) {
            failed += ArraySizeMismatch(expected.size(), actual.size(), expected, actual, path)
            if (arrayComparisonMode != ArrayComparisonMode.PERMUTATION_BASED) {
                return
            }
        }

        return when (arrayComparisonMode) {
            ArrayComparisonMode.PERMUTATION_BASED -> {
                val paddedActual = actual.toList().padTo(expected.size(), MissingNode.getInstance())
                val paddedExpected = expected.toList().padTo(actual.size(), MissingNode.getInstance())

                val mismatches = paddedActual.asSequence()
                    .zipWithIndex()
                    .permutations()
                    .crossProduct(paddedExpected.asSequence().zipWithIndex().permutations())
                    .map { (actualPermutations, expectedPermutations) ->
                        mutableListOf<ComparisonMismatch>().apply {
                            actualPermutations.zip(expectedPermutations).forEach { (a, e) ->
                                val newPath = path
                                    .appendExpected(e.first.toString())
                                    .appendActual(a.first.toString())
                                diff(e.second, a.second, newPath, this)
                            }
                        }
                    }
                    .minBy { it.size } ?: emptyList<ComparisonMismatch>()

                failed += mismatches
            }
            else -> {
                sort(actual.zipWithIndex()).asSequence()
                    .zip(sort(expected.zipWithIndex()).asSequence())
                    .forEach { (actual, expected) ->
                        val newPath = path
                            .appendActual(actual.first.toString())
                            .appendExpected(expected.first.toString())
                        diff(expected.second, actual.second, newPath, failed)
                    }
            }
        }
    }
}
