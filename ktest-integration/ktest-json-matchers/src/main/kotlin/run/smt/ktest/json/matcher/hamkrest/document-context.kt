package run.smt.ktest.json.matcher.hamkrest

import com.fasterxml.jackson.databind.JsonNode
import com.jayway.jsonpath.DocumentContext
import run.smt.ktest.json.matcher.api.MatcherConfig
import run.smt.ktest.json.matcher.api.MatcherConfigDSL
import run.smt.ktest.json.matcher.api.matcherConfig

class ConfiguredDocumentContextMatcher(private val config: MatcherConfig) {
    fun matches(node: JsonNode) = DocumentContextWithJsonNodeMatcher(node, config)
    fun matches(dc: DocumentContext) = DocumentContextWithDocumentContextMatcher(dc, config)

    val JsonNode.json : DocumentContextWithJsonNodeMatcher get() = DocumentContextWithJsonNodeMatcher(this, config)
    val DocumentContext.json : DocumentContextWithDocumentContextMatcher get() = DocumentContextWithDocumentContextMatcher(this, config)
}

fun documentContextMatcherConfig(dsl: MatcherConfigDSL) = documentContextMatcherConfig(matcherConfig(dsl))
fun documentContextMatcherConfig(config: MatcherConfig) = ConfiguredDocumentContextMatcher(config)
object DocumentContextMatchers {
    private val sortAwareDocumentContextConfig = documentContextMatcherConfig {
        strictlyCompareArrays()
    }

    private val sortAgnosticDocumentContextConfig = documentContextMatcherConfig {
        compareArraysUnordered()
    }

    fun isIdenticalTo(other: JsonNode) = sortAwareDocumentContextConfig.matches(other)
    fun isIdenticalTo(other: DocumentContext) = sortAwareDocumentContextConfig.matches(other)
    fun matches(other: JsonNode) = sortAgnosticDocumentContextConfig.matches(other)
    fun matches(other: DocumentContext) = sortAgnosticDocumentContextConfig.matches(other)

    fun unordered(other: JsonNode) = sortAgnosticDocumentContextConfig.matches(other)
    fun unordered(other: DocumentContext) = sortAgnosticDocumentContextConfig.matches(other)
    fun ordered(other: JsonNode) = sortAwareDocumentContextConfig.matches(other)
    fun ordered(other: DocumentContext) = sortAwareDocumentContextConfig.matches(other)
}
