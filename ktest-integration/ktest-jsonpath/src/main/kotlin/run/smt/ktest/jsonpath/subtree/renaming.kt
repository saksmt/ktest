package run.smt.ktest.jsonpath.subtree

import com.jayway.jsonpath.DocumentContext

data class RenameRule internal constructor(internal val from: String, internal val to: String)

typealias SubtreeRenameSpec = Set<RenameRule>
typealias SubtreeRenameDSL = SubtreeRenameSpecBuilder.() -> Unit

class SubtreeRenameSpecBuilder internal constructor(basePath: String) : AbstractSubtreeBuilder<SubtreeRenameSpecBuilder, RenameRule>(basePath) {
    infix fun String.to(other: String) {
        if ('.' in other) {
            throw IllegalArgumentException("Can not move nodes around, only renaming allowed. Wanted to move from $this to $other")
        }

        addValue(RenameRule(this, other))
    }

    override fun RenameRule.prependToPath(what: String): RenameRule {
        return copy(from = what + from)
    }

    override fun RenameRule.prependBasePath(basePath: String): RenameRule {
        return copy(from = basePath + from)
    }

    override fun newInstance(newBasePath: String) = SubtreeRenameSpecBuilder(newBasePath)
    override fun self() = this
}

val createRenameRules = createSubtreeCreator(::SubtreeRenameSpecBuilder)

fun DocumentContext.rename(vararg fields: Pair<String, String>) = rename {
    fields.forEach { (old, new) ->
        old to new
    }
}

fun DocumentContext.rename(dsl: SubtreeRenameDSL) = rename(createRenameRules(dsl))

fun DocumentContext.rename(fields: SubtreeRenameSpec): DocumentContext {
    return fields
        .filter { (oldPath, newName) -> oldPath.substring(oldPath.lastIndexOf(".") + 1) != newName }
        .fold(this) { acc, (oldFieldPath, newName) ->
            val (parent, oldName) = extractParentAndNode(oldFieldPath)
            acc.renameKey(parent, oldName, newName)
        }
}
