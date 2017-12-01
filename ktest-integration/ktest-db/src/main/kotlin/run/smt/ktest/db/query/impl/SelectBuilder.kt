package run.smt.ktest.db.query.impl

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

/**
 * Represents SELECT query
 */
class SelectBuilder<T : Any>(_query: String? = null) : RespondingQueryImpl<T>(_query) { init {

    singleLoader { jdbcTemplate ->
        val jdbc = NamedParameterJdbcTemplate(jdbcTemplate)
        if (forPrimitive) {
            jdbc.queryForObject(query, parameterImplementation(), mapTo.java)
        } else {
            jdbc.queryForObject(query, parameterImplementation(), SimpleRowMapper(mapTo))
        }
    }

    listLoader { jdbcTemplate ->
        val jdbc = NamedParameterJdbcTemplate(jdbcTemplate)
        if (forPrimitive) {
            jdbc.queryForList(query, parameterImplementation(), mapTo.java)
        } else {
            jdbc.query(query, parameterImplementation(), SimpleRowMapper(mapTo))
        }
    }

    mapLoader { jdbcTemplate ->
        NamedParameterJdbcTemplate(jdbcTemplate).queryForMap(query, parameterImplementation())
    }
} }
