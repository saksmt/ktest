package run.smt.ktest.db.query

import java.sql.JDBCType

interface PlainProcedureBuilder : QueryBuilder {
    /**
     * Procedure output parameters
     */
    val outParameters: MutableMap<String, Int>
    /**
     * Output type for procedures with one output parameter
     */
    var outputSqlType: Int?

    /**
     * Register new output parameter
     */
    fun outParameter(name: String, jdbcType: JDBCType)

    class BadMappingException(message: String) : RuntimeException(message)
}
