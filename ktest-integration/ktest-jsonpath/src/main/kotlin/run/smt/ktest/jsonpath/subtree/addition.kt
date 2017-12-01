package run.smt.ktest.jsonpath.subtree

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.jayway.jsonpath.DocumentContext
import run.smt.ktest.jsonpath.castTo
import run.smt.ktest.jsonpath.get
import run.smt.ktest.util.collection.scan
import run.smt.ktest.util.collection.tail

data class PutFieldRule internal constructor(internal val path: String, internal val what: Any?)

typealias PutFieldSpec = Set<PutFieldRule>
typealias PutFieldDSL = PutFieldSpecBuilder.() -> Unit

class PutFieldSpecBuilder(basePath: String) : AbstractSubtreeBuilder<PutFieldSpecBuilder, PutFieldRule>(basePath) {
    infix fun <T> T.at(path: String) {
        addValue(PutFieldRule(path, this))
    }

    override fun PutFieldRule.prependToPath(what: String): PutFieldRule {
        return copy(path = what + path)
    }

    override fun PutFieldRule.prependBasePath(basePath: String): PutFieldRule {
        return copy(path = basePath + path)
    }

    override fun newInstance(newBasePath: String) = PutFieldSpecBuilder(newBasePath)
    override fun self(): PutFieldSpecBuilder = this
}

val createPutRules = createSubtreeCreator(::PutFieldSpecBuilder)

fun DocumentContext.put(force: Boolean = false, dsl: PutFieldDSL) = put(createPutRules(dsl), force)

fun DocumentContext.put(spec: PutFieldSpec, force: Boolean = false): DocumentContext {
    return spec.fold(this) { acc, (path, value) ->
        val (parentPath, name) = extractParentAndNode(path)
        if (force) {
            enforceToBeObject(parentPath)
        }
        acc.put(parentPath, name, value)
    }
}

private fun DocumentContext.enforceToBeObject(path: String) {
    val pathExceptRoot = path.split(".").tail()
    val parentsExceptRoot = pathExceptRoot.scan("$") { p, n -> p + "." + n }.tail()

    val resolvableContext = asResolvableContext(ignoreMissing = true)

    parentsExceptRoot.forEach { parentPath ->
        val (parentOfParent, node) = extractParentAndNode(parentPath)

        // when no parent exists, create new object node
        if (resolvePaths(setOf(JsonPathSpec(parentPath)), resolvableContext).isEmpty()) {
            put(parentOfParent, node, mutableMapOf<String, Any>())
            // else if there is some parent and it is not and object node, remove and create new node
        } else if (this[parentPath] castTo JsonNode::class !is ObjectNode) {
            remove { + parentPath.replaceFirst("$.", "") }
            put(parentOfParent, node, mutableMapOf<String, Any>())
        }
    }
}
