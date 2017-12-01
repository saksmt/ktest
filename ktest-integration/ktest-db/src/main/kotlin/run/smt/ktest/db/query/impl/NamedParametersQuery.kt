package run.smt.ktest.db.query.impl

import org.slf4j.LoggerFactory
import java.sql.CallableStatement
import java.sql.Connection
import java.sql.Date as SqlDate
import java.util.Date as JavaDate

enum class SqlParamType { IN, OUT }
data class SqlParam(val index: Int, val name: String, val position: IntRange, val value: Any?, val type: SqlParamType)

/**
 * Represents query originally with named parameters in JDBC acceptable format - index based query
 */
class NamedParametersQuery(query: String, private val outParams: Map<String, Int>) {
    private var _bindings: Sequence<SqlParam> = parseParams(query)
    val bindings: Sequence<SqlParam>
        get() = _bindings
    private val parsedQuery = parse(query)

    companion object {
        private val log = LoggerFactory.getLogger(NamedParametersQuery::class.java)

        private val PARAM_REGEX = "[^\"'][:](\\w+)".toRegex()
        private val QUOTE_REGEX = "[\"\']".toRegex()
        private val INDEX_PLACEHOLDER = "?"
    }

    /**
     * Create new callable statement with named parameters replaced with indexes
     */
    fun createCallableStatement(connection: Connection, inParams: Map<String, Any?>): CallableStatement {
        log.debug("Preparing callable query: {}", parsedQuery)
        val cs = connection.prepareCall(parsedQuery) ?: throw IllegalStateException("Failed to prepare call!")
        registerOutParameters(cs)
        fillInParameter(cs, inParams)
        return cs
    }

    /**
     * Extracts parameters from original query string
     */
    private fun parseParams(query: String): Sequence<SqlParam> {
        return PARAM_REGEX.findAll(query).asSequence()
            .map { it.groups[1]!! }
            .filter { QUOTE_REGEX !in query lineContaining it.range }
            .filterNot { it.range.commentedOutIn(query) }
            .sortedBy { it.range.first }
            .mapIndexed { i, v -> createParam(i, v) }
    }

    private fun createParam(i: Int, v: MatchGroup) = SqlParam(i + 1, v.value, v.range, null, typeOf(v.value))

    /**
     * Get SqlParamType based on presence of name in list of output parameters
     */
    private fun typeOf(name: String) =
        if (outParams.containsKey(name)) SqlParamType.OUT else SqlParamType.IN

    /**
     * Replace all names with indexes in given query
     */
    private fun parse(query: String): String {
        return bindings.fold(query to 0) { acc, currentParam ->

            val (oldQuery, offset) = acc
            val startIncludingColon = currentParam.position.start - 1
            val offsetPosition = IntRange(startIncludingColon - offset, currentParam.position.endInclusive - offset)
            val newQuery = oldQuery.replaceRange(offsetPosition, INDEX_PLACEHOLDER)

            newQuery to offset + (currentParam.position.endInclusive - startIncludingColon)

        }.first
    }

    /**
     * Set all input parameters into given callable statement
     */
    private fun fillInParameter(cs: CallableStatement, inParams: Map<String, Any?>) {
        fun updateValue(sqlParam: SqlParam): SqlParam =
            if (sqlParam.type == SqlParamType.IN)
                sqlParam.copy(value = prepareData(inParams[sqlParam.name]))
            else
                sqlParam

        _bindings = bindings.map(::updateValue)

        bindings
            .filter { it.type == SqlParamType.IN }
            .forEach {
                log.debug("Setting IN parameter (namely \"{}\") at index {}: {}", it.name, it.index, it.value)
                cs.setObject(it.index, it.value)
            }
    }

    /**
     * Register all output parameters in given callable statement
     */
    private fun registerOutParameters(cs: CallableStatement) {
        bindings
            .filter { it.type == SqlParamType.OUT }
            .forEach {
                log.debug("Registering OUT parameter (namely \"{}\") at index {}", it.name, it.index)
                cs.registerOutParameter(
                    it.index,
                    outParams[it.name]
                        ?: throw IllegalArgumentException("No type provided for OUT parameter named ${it.name}")
                )
            }
    }

    private fun prepareData(v: Any?): Any? {
        return when (v) {
            is java.util.Date -> java.sql.Date(v.time)
            else -> v
        }
    }
}

/**
 * Find whole line containing specified range, i.e. if range is in the middle of line 2 give the whole second line
 */
private infix fun String.lineContaining(range: IntRange): String =
    substring(dropLast(length - range.endInclusive).lastIndexOf('\n'), indexOf('\n', range.endInclusive))

/**
 * Check if code in specified range is commented out
 */
private fun IntRange.commentedOutIn(code: String, singleLineComment: String = "--", multiLineComment: Pair<String, String> = "/*" to "*/"): Boolean {

    fun IntRange.singleLine(): Boolean {
        val lineToInspect = code lineContaining this
        val commentStart = lineToInspect.indexOf(singleLineComment)
        return commentStart > 0 && commentStart < start
    }

    fun IntRange.multiLine(): Boolean {
        val inspectedCode = code.dropLast(code.length - endInclusive)
        val commentStart = inspectedCode.lastIndexOf(multiLineComment.first)
        val commentEnd = inspectedCode.lastIndexOf(multiLineComment.second)
        return commentStart > commentEnd && start > commentStart && !(commentStart..commentStart).singleLine()
    }

    return singleLine() || multiLine()
}
