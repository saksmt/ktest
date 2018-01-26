package run.smt.ktest.db.query

import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource

fun DataSource.ddl(sql: String) {
    JdbcTemplate(this).execute(sql)
}
