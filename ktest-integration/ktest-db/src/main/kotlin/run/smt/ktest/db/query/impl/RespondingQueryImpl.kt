package run.smt.ktest.db.query.impl

import run.smt.ktest.db.query.RespondingQuery
import run.smt.ktest.db.query.Response
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet
import kotlin.reflect.KClass

/**
 * Represents query that returns some result
 */
abstract class RespondingQueryImpl<T : Any>(_query: String?) : QueryBuilderImpl(_query), RespondingQuery<T> {
    private var _mapTo: KClass<T>? = null

    /**
     * Function extracting list of requested values from spring-jdbc
     */
    private lateinit var listLoader: (JdbcTemplate) -> List<T>

    /**
     * Function extracting result as map from spring-jdbc
     */
    private lateinit var mapLoader: (JdbcTemplate) -> Map<String, Any>?

    /**
     * Function extracting single POJO from spring-jdbc
     */
    private lateinit var singleLoader: (JdbcTemplate) -> T?

    /**
     * Class to map result to
     */
    override var mapTo: KClass<T>
        get() = _mapTo ?: throw IllegalStateException("No class present to map to")
        set(value) {
            _mapTo = value
        }

    override fun mapTo(clazz: KClass<T>) {
        _mapTo = clazz
    }

    /**
     * Whether result is primitive type
     */
    protected val forPrimitive: Boolean get() {
        return mapTo.javaPrimitiveType != null || mapTo == String::class
    }

    protected fun listLoader(loader: (JdbcTemplate) -> List<T>) {
        listLoader = loader
    }

    protected fun mapLoader(loader: (JdbcTemplate) -> Map<String, Any>?) {
        mapLoader = loader
    }

    protected fun singleLoader(loader: (JdbcTemplate) -> T?) {
        singleLoader = loader
    }

    /**
     * Execute query
     */
    fun execute(jdbc: JdbcTemplate): Response<T> {
        return BaseResponse(
            mapTo,
            jdbc,
            listLoader,
            mapLoader,
            singleLoader
        )
    }

    /**
     * Adapter for spring-jdbc `RowMapper`
     */
    protected class SimpleRowMapper<T : Any>(mapTo: KClass<T>) : RowMapper<T> {
        private val mapper = Mapper(mapTo)

        override fun mapRow(rs: ResultSet?, rowNum: Int): T?
            = mapper.map(rs?.let { SqlOutputAdapter(rs) })
    }

}
