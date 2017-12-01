package run.smt.ktest.jsonpath

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.Predicate
import run.smt.ktest.jsonpath.criteria.CriteriaDSL
import run.smt.ktest.jsonpath.criteria.filter

data class SqlLikeDSL internal constructor(
    internal val context: DocumentContext?,
    internal val what: String?
)

fun select(what: String): SqlLikeDSL = SqlLikeDSL(null, what)
infix fun DocumentContext.select(what: String): SqlLikeDSL = SqlLikeDSL(this, what)
infix fun SqlLikeDSL.from(dc: DocumentContext) = copy(context = dc)
infix fun SqlLikeDSL.where(criteriaBuilder: CriteriaDSL): DocumentContext = this where filter(criteriaBuilder)
infix fun SqlLikeDSL.where(predicate: Predicate): DocumentContext {
    return (context ?: throw IllegalStateException()) [
        what ?: throw IllegalStateException(), predicate
    ]
}
