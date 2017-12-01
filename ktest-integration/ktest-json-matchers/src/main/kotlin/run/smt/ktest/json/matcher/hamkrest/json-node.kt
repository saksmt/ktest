package run.smt.ktest.json.matcher.hamkrest

import com.fasterxml.jackson.databind.JsonNode
import com.jayway.jsonpath.DocumentContext
import run.smt.ktest.json.matcher.api.MatcherConfig
import run.smt.ktest.json.matcher.api.MatcherConfigDSL
import run.smt.ktest.json.matcher.api.matcherConfig

class ConfiguredJsonNodeMatcher(private val config: MatcherConfig) {
    fun matches(node: JsonNode) = JsonNodeWithJsonNodeMatcher(node, config)
    fun matches(dc: DocumentContext) = JsonNodeWithDocumentContextMatcher(dc, config)

    val JsonNode.json : JsonNodeWithJsonNodeMatcher get() = JsonNodeWithJsonNodeMatcher(this, config)
    val DocumentContext.json : JsonNodeWithDocumentContextMatcher get() = JsonNodeWithDocumentContextMatcher(this, config)
}

fun jsonNodeMatcherConfig(dsl: MatcherConfigDSL) = jsonNodeMatcherConfig(matcherConfig(dsl))
fun jsonNodeMatcherConfig(matcherConfig: MatcherConfig) = ConfiguredJsonNodeMatcher(matcherConfig)
object JsonNodeMatchers {
    private val sortAwareJsonJsonNodeConfig = jsonNodeMatcherConfig {
        strictlyCompareArrays()
    }

    private val sortAgnosticJsonNodeConfig = jsonNodeMatcherConfig {
        compareArraysUnordered()
    }

    fun isIdenticalTo(other: JsonNode) = sortAwareJsonJsonNodeConfig.matches(other)
    fun isIdenticalTo(other: DocumentContext) = sortAwareJsonJsonNodeConfig.matches(other)
    fun matches(other: JsonNode) = sortAgnosticJsonNodeConfig.matches(other)
    fun matches(other: DocumentContext) = sortAgnosticJsonNodeConfig.matches(other)

    fun unordered(other: JsonNode) = sortAgnosticJsonNodeConfig.matches(other)
    fun unordered(other: DocumentContext) = sortAgnosticJsonNodeConfig.matches(other)
    fun ordered(other: JsonNode) = sortAwareJsonJsonNodeConfig.matches(other)
    fun ordered(other: DocumentContext) = sortAwareJsonJsonNodeConfig.matches(other)
}

