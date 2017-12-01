package run.smt.ktest.db.query

import run.smt.ktest.db.query.impl.ProcedureBuilderImpl
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource
import java.sql.Date as SqlDate

/**
 * Callable query
 */
inline fun <reified T : Any> DataSource.call(query: String? = null, build: ProcedureBuilder<T>.() -> Unit): Response<T> {
    val builder = ProcedureBuilderImpl<T>(query)
    builder.mapTo = T::class
    builder.build()
    return builder.execute(JdbcTemplate(this))
}

inline fun <reified T : Any> DataSource.call(query: String): Response<T> = call(query) {}

fun DataSource.execute(procedure: String?, build: PlainProcedureBuilder.() -> Unit) {
    call<NoArg>(procedure, build).single()
}

fun DataSource.execute(procedure: String) = execute(procedure) {}

private class NoArg
