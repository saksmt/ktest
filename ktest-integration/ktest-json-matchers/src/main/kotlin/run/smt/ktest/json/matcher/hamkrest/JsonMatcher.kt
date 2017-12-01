package run.smt.ktest.json.matcher.hamkrest

import com.fasterxml.jackson.databind.JsonNode
import com.jayway.jsonpath.DocumentContext
import com.natpryce.hamkrest.MatchResult
import com.natpryce.hamkrest.Matcher
import run.smt.ktest.json.mapTo
import run.smt.ktest.json.matcher.api.ComparisonMismatch
import run.smt.ktest.json.matcher.api.MatcherConfig
import run.smt.ktest.json.matcher.api.jsonComparatorFor
import run.smt.ktest.jsonpath.castTo
import run.smt.ktest.jsonpath.subtree.*
import run.smt.ktest.util.functional.Either.*
import run.smt.ktest.util.functional.identity

abstract class JsonMatcher<T, S: JsonMatcher<T, S>>(private val config: MatcherConfig, private val expected: Either<JsonNode, DocumentContext>) : Matcher<T> {
    private var preProcess: ((DocumentContext) -> DocumentContext)? = null
    protected abstract val self: S

    infix fun bySubtree(subtreeSpec: SubtreeSpec): S {
        preProcess = { extractSubtree(it, subtreeSpec) }
        return self
    }

    infix fun afterRemovalOfSubtree(subtreeSpec: SubtreeSpec): S {
        preProcess = { it.remove(subtreeSpec) }
        return self
    }

    infix fun bySubtree(subtreeDSL: SubtreeDSL) = bySubtree(createSubtree(subtreeDSL))
    infix fun afterRemovalOfSubtree(subtreeDSL: SubtreeDSL) = afterRemovalOfSubtree(createSubtree(subtreeDSL))

    override val description: String
        get() = if (preProcess == null) {
            "equal to expected JSON"
        } else {
            "match expected JSON by specified preProcess"
        }

    protected fun check(actualEither: Either<JsonNode, DocumentContext>): MatchResult {
        val comparator = jsonComparatorFor(config)

        val actualPreProcess = preProcess
        val mismatches = if (actualPreProcess == null) {
            val expected = this.expected.unify(identity(), { it castTo JsonNode::class })
            val actual = actualEither.unify(identity(), { it castTo JsonNode::class })

            comparator.diff(expected, actual)
        } else {
            val expected = actualPreProcess(this.expected.unify({ it mapTo DocumentContext::class }, identity()))
            val actual  = actualPreProcess(actualEither.unify({ it mapTo DocumentContext::class }, identity()))

            comparator.diff(expected castTo JsonNode::class, actual castTo JsonNode::class)
        }

        return if (mismatches.isEmpty()) {
            MatchResult.Match
        } else {
            MatchResult.Mismatch(generateDescription(mismatches))
        }
    }

    private fun generateDescription(mismatches: List<ComparisonMismatch>): String {
        val mismatchesToPrint = if (config.printFirstNMismatches > 0) mismatches.take(config.printFirstNMismatches) else mismatches
        return mismatchesToPrint.joinToString("\n") { it.message }
    }
}

class JsonNodeWithJsonNodeMatcher(expected: JsonNode, config: MatcherConfig) : JsonMatcher<JsonNode, JsonNodeWithJsonNodeMatcher>(config, left(expected)) {
    override val self = this
    override fun invoke(actual: JsonNode) = check(left(actual))
}

class DocumentContextWithJsonNodeMatcher(expected: JsonNode, config: MatcherConfig) : JsonMatcher<DocumentContext, DocumentContextWithJsonNodeMatcher>(config, left(expected)) {
    override val self = this
    override fun invoke(actual: DocumentContext) = check(right(actual))
}

class JsonNodeWithDocumentContextMatcher(expected: DocumentContext, config: MatcherConfig) : JsonMatcher<JsonNode, JsonNodeWithDocumentContextMatcher>(config, right(expected)) {
    override val self = this
    override fun invoke(actual: JsonNode) = check(left(actual))
}

class DocumentContextWithDocumentContextMatcher(expected: DocumentContext, config: MatcherConfig) : JsonMatcher<DocumentContext, DocumentContextWithDocumentContextMatcher>(config, right(expected)) {
    override val self = this
    override fun invoke(actual: DocumentContext) = check(right(actual))
}
