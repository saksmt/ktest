package run.smt.ktest.db.query.impl

import run.smt.ktest.db.mapping.Column
import run.smt.ktest.db.query.PlainProcedureBuilder
import run.smt.ktest.db.query.ProcedureBuilder
import org.springframework.jdbc.core.CallableStatementCallback
import org.springframework.jdbc.core.CallableStatementCreator
import org.springframework.jdbc.core.JdbcTemplate
import run.smt.ktest.util.tomap.toMap
import java.sql.CallableStatement
import java.sql.JDBCType
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaMethod

/**
 * Represents callable query (procedure, anonymous block)
 */
class ProcedureBuilderImpl<T : Any>(query: String?) : RespondingQueryImpl<T>(query), ProcedureBuilder<T> {

    /**
     * Procedure output parameters
     */
    override val outParameters: MutableMap<String, Int> = mutableMapOf()

    /**
     * Output type for procedures with one output parameter
     */
    override var outputSqlType: Int? = null

    /**
     * Register new output parameter
     */
    override fun outParameter(name: String, jdbcType: JDBCType) {
        outParameters[name] = jdbcType.vendorTypeNumber
    }

    /**
     * Extract parameters from given POJO
     */
    override fun parametersFrom(pojo: Any) {
        parameters.putAll(pojo.toMap())
    }

    /**
     * Check if property can accept value from column with given name
     */
    private fun <T, R> KProperty1<T, R>.acceptsColumn(column: String): Boolean
        = name == column || annotations.filterIsInstance(Column::class.java).any { it.name == column }

    init {
        /**
         * Mapper for single entity
         */
        singleLoader { jdbc ->
            doExec(jdbc) { cs, bindings ->
                val result = mapTo.java.newInstance()
                val map = extractMap(cs, bindings)
                val properties = mapTo.memberProperties
                for ((key, value) in map) {
                    val propertyToMap = properties.find { it.acceptsColumn(key) } ?: continue
                    @Suppress("UNCHECKED_CAST") val castProperty = propertyToMap as? KMutableProperty1<T, Any?> ?: continue
                    val pojoValueType = castProperty.setter.javaMethod?.parameters?.get(0)?.type

                    if (!(pojoValueType?.isAssignableFrom(value.javaClass) ?: false)) {
                        throw PlainProcedureBuilder.BadMappingException("Can not assign ${value.javaClass.canonicalName} to method accepting " +
                            "${pojoValueType?.canonicalName}: ${mapTo.qualifiedName}#$key")
                    }
                    castProperty.set(result, value)
                }
                result
            }
        }

        mapLoader { jdbc ->
            doExec(jdbc) { cs, bindings ->
                extractMap(cs, bindings)
            }
        }

        listLoader { throw UnsupportedOperationException("Stored procedure can not return list of values!") }
    }

    /**
     * Extract ColumnName -> ColumnValue map from callable statement based on list of defined SQL paramters (both IN and OUT)
     */
    private fun extractMap(cs: CallableStatement, bindings: Sequence<SqlParam>): Map<String, Any> {
        fun getValue(sqlParam: SqlParam): Any? {
            return if (sqlParam.type == SqlParamType.IN) {
                sqlParam.value
            } else {
                cs.getObject(sqlParam.index)
            }
        }

        return bindings
            .map { it.name to getValue(it) }
            .groupBy({ it.first }, { it.second })
            .mapValues { it.value.firstOrNull() }
            .filterValues { it != null }
            .mapValues { it.value!! }
    }

    /**
     * Template method: parse source query -> execute prepared query -> map result to output POJO based on given mapper
     */
    private fun <T> doExec(jdbc: JdbcTemplate, map: (CallableStatement, Sequence<SqlParam>) -> T?): T? {
        val queryObject = NamedParametersQuery(query, outParameters)

        val result: T? =
            jdbc.execute(CallableStatementCreator {
                queryObject.createCallableStatement(it, parameters)
            }, CallableStatementCallback {
                it.execute()
                map(it, queryObject.bindings)
            })

        return result
    }
}
