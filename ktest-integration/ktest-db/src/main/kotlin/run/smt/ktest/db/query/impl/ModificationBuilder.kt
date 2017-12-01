package run.smt.ktest.db.query.impl

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

/**
 * Represents query that performs some modification (thus returns nothing)
 */
class ModificationBuilder(_query: String?) : QueryBuilderImpl(_query) {
    internal fun exec(jdbc: NamedParameterJdbcTemplate) = jdbc.update(query, parameterImplementation())
}
