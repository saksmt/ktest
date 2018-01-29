package run.smt.ktest.jsonpath.subtree

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.Option
import run.smt.ktest.jsonpath.castTo
import run.smt.ktest.jsonpath.copy

internal fun resolvePaths(spec: SubtreeSpec, resolvableContext: DocumentContext): Set<String> {
    return spec.asSequence()
        .map { it.applyOn(resolvableContext) }
        .map { it.castTo { list<String>() } }
        .flatMap { it.asSequence() }
        .toSet()
}

internal fun DocumentContext.asResolvableContext(ignoreMissing: Boolean = false): DocumentContext {
    val additionalOptions = if (ignoreMissing) arrayOf(Option.SUPPRESS_EXCEPTIONS) else arrayOf()
    return copy(*(arrayOf(Option.AS_PATH_LIST) + additionalOptions))
}

internal fun Sequence<String>.removeFrom(dc: DocumentContext): DocumentContext {
    return sortedBy { it.length }
        .fold(dc to emptyList<String>()) { acc, candidateForRemoval ->
            val (newDc, processed) = acc
            // we need to do only top-level removals
            if (processed.none { candidateForRemoval.startsWith(it) }) {
                newDc.delete(candidateForRemoval)
                newDc to (processed + candidateForRemoval)
            } else {
                acc
            }
        }.first
}

internal fun extractParentAndNode(path: String): Pair<String, String> {
    val splitted = path.split('.')
    val parent = splitted.dropLast(1).joinToString(".")
    val node = splitted.last()
    return parent to node
}
