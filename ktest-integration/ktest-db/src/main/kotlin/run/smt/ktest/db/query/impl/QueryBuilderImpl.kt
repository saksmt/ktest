package run.smt.ktest.db.query.impl

import run.smt.ktest.db.query.QueryBuilder
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.SqlParameterSource
import java.sql.Date

/**
 * Represents SQL query
 */
abstract class QueryBuilderImpl(private var _query: String?) : QueryBuilder {
    /**
     * Which implementation from spring-jdbc to use for parameters
     */
    protected var parameterImplementation: () -> SqlParameterSource = { MapSqlParameterSource(parameters) }

    /**
     * Map of parameters
     */
    override var parameters: MutableMap<String, Any?> = mutableMapOf()

    /**
     * Query string
     */
    override var query: String
        get() = _query ?: throw IllegalStateException("No query string present")
        set(value) {
            _query = value
        }

    /**
     * Add/overwrite parameter, exclusive with `parametersFrom`
     */
    override fun parameter(name: String, value: Any?) {
        parameters[name] = value
    }

    /**
     * Extract parameters from given pojo, exclusive with `parameter`
     */
    override fun parametersFrom(pojo: Any) {
        parameterImplementation = { BeanPropertySqlParameterSource(pojo) }
    }

    /**
     * Converts some complex types into JDBC representation
     */
    protected fun prepareData(v: Any?): Any? {
        return when (v) {
            is java.util.Date -> Date(v.time)
            else -> v
        }
    }
}
