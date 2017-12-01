package run.smt.ktest.db.query

import run.smt.ktest.db.query.impl.SelectBuilder
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource

inline fun <reified T : Any> DataSource.select(query: String): Response<T> {
    return select(query) {}
}

inline fun <reified T : Any> DataSource.select(query: String? = null, build: SelectBuilder<T>.() -> Unit): Response<T> {
    val builder = SelectBuilder<T>(query)
    builder.mapTo(T::class)
    builder.build()
    return builder.execute(JdbcTemplate(this))
}
