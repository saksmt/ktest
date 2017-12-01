package run.smt.ktest.jsonpath.subtree

import com.jayway.jsonpath.DocumentContext
import run.smt.ktest.jsonpath.castTo
import run.smt.ktest.jsonpath.get

fun extractSubtree(dc: DocumentContext, ignoreMissing: Boolean = false, dsl: SubtreeDSL) = extractSubtree(dc, createSubtree(dsl), ignoreMissing)

fun extractSubtree(dc: DocumentContext, spec: SubtreeSpec, ignoreMissing: Boolean = false): DocumentContext {
    val resolvableDc = dc.asResolvableContext(ignoreMissing)
    val allPaths = resolvableDc["$..*"].castTo<List<String>> { list<String>() }.asSequence()

    val preservedPaths = resolvePaths(spec, resolvableDc)

    return allPaths
        .filter { candidateForRemoval ->
            // excluding all parents for preserved nodes from list for removal
            preservedPaths.none { preserved ->
                preserved.startsWith(candidateForRemoval)
            }
        }
        .filter { candidateForRemoval ->
            // excluding all children for preserved nodes from list for removal
            preservedPaths.none { preserved ->
                candidateForRemoval.startsWith(preserved)
            }
        }
        .removeFrom(dc)
}
