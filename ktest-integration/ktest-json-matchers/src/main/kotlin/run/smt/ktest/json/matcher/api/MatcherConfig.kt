package run.smt.ktest.json.matcher.api

import run.smt.ktest.config.config
import run.smt.ktest.config.get

class MatcherConfig internal constructor(
    internal val arrayComparisonMode: ArrayComparisonMode,
    internal val strictNullChecking: Boolean,
    internal val fieldDifferenceThreshold: Int,
    internal val strictObjectFields: Boolean,
    val printFirstNMismatches: Int
) {
    internal val comparator by lazy { JsonComparator(
        arrayComparisonMode,
        strictNullChecking,
        strictObjectFields,
        fieldDifferenceThreshold
    ) }
}

typealias MatcherConfigDSL = MatcherConfigBuilder.() -> Unit
class MatcherConfigBuilder internal constructor() {
    internal var arrayComparisonMode: ArrayComparisonMode = arrayComparisonModeFromConfig()
    internal var strictNullChecking: Boolean = config["json.comparator.strict-null-checking"]
    internal var fieldDifferenceThreshold: Int = config["json.comparator.object-difference-threshold"]
    internal var strictObjectFields: Boolean = config["json.comparator.strict-object-field-checking"]
    internal var printFirstNMismatches: Int = config["json.comparator.print-first-n-mismatches"]

    fun strictlyCompareArrays() {
        arrayComparisonMode = ArrayComparisonMode.STRICT
    }

    fun compareArraysUnordered() {
        arrayComparisonMode = ArrayComparisonMode.UNORDERED
    }

    fun checkAllPermutationsOnArrayComparison() {
        arrayComparisonMode = ArrayComparisonMode.PERMUTATION_BASED
    }

    fun compareArraysBasedOnPermutations() {
        checkAllPermutationsOnArrayComparison()
    }

    fun compareNullsStrictly() {
        strictNullChecking = true
    }

    fun compareNullsNonStrictly() {
        strictNullChecking = false
    }

    fun missingFieldsEqualToNulls() {
        strictNullChecking = false
    }

    fun giveUpComparisonOn(nDifferentFields: Int) {
        fieldDifferenceThreshold = nDifferentFields
    }

    fun giveUpComparisonWhenObjectStructureDiffers(atMost: Int = 0) {
        fieldDifferenceThreshold = atMost
    }

    fun neverGiveUpComparisonOfObjects() {
        fieldDifferenceThreshold = Int.MAX_VALUE
    }

    fun extraFieldsInObjectAreAllowed() {
        strictObjectFields = false
    }

    fun extraFieldsInObjectAreNotAllowed() {
        strictObjectFields = true
    }

    fun printOnlyFirstMismatches(nMismatches: Int) {
        printFirstNMismatches = nMismatches
    }

    fun printAllMismatches() {
        printFirstNMismatches = -1
    }

    fun printOnlyFirstMismatch() {
        printFirstNMismatches = 1
    }

    private fun arrayComparisonModeFromConfig(): ArrayComparisonMode {
        val originalValue = config.get<String>("json.comparator.array-comparison")
        val preparedValue = originalValue.toUpperCase().replace('-', '_')

        return ArrayComparisonMode.valueOf(if (preparedValue == "PERMUTATION") "PERMUTATION_BASED" else preparedValue)
    }
}

fun matcherConfig(dsl: MatcherConfigDSL) = MatcherConfigBuilder().apply(dsl).run {
    MatcherConfig(arrayComparisonMode, strictNullChecking, fieldDifferenceThreshold, strictObjectFields, printFirstNMismatches)
}

fun jsonComparatorFor(config: MatcherConfig) = config.comparator
fun jsonComparator(dsl: MatcherConfigDSL = {}) = jsonComparatorFor(matcherConfig(dsl))
