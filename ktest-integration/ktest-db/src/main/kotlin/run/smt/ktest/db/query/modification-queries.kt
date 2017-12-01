package run.smt.ktest.db.query

import run.smt.ktest.db.query.impl.ModificationBuilder
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import javax.sql.DataSource

/**
 * INSERT query
 */
fun DataSource.insert(query: String? = null, build: ModificationBuilder.() -> Unit) {
    val builder = ModificationBuilder(query)
    builder.build()
    builder.exec(NamedParameterJdbcTemplate(this))
}

/**
 * INSERT query
 */
fun DataSource.insert(query: String) = insert(query) {}

/**
 * UPDATE query
 */
fun DataSource.update(query: String? = null, build: ModificationBuilder.() -> Unit) = insert(query, build)
fun DataSource.update(query: String) = insert(query)

/**
 * DELETE query
 */
fun DataSource.delete(query: String? = null, build: ModificationBuilder.() -> Unit) = insert(query, build)
fun DataSource.delete(query: String) = insert(query)
