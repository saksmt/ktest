package run.smt.ktest.jsonpath.subtree

import com.jayway.jsonpath.DocumentContext

fun DocumentContext.remove(vararg paths: String, ignoreMissing: Boolean = false) = remove(ignoreMissing) {
    paths.forEach { + it }
}

fun DocumentContext.remove(ignoreMissing: Boolean = false, dsl: SubtreeDSL) = remove(createSubtree(dsl), ignoreMissing = ignoreMissing)

fun DocumentContext.remove(vararg paths: SubtreeSpec, ignoreMissing: Boolean = false): DocumentContext {
    val resolvableDc = asResolvableContext(ignoreMissing)
    return paths.asSequence()
        .map { resolvePaths(it, resolvableDc) }
        .flatMap { it.asSequence() }
        .removeFrom(this)
}
