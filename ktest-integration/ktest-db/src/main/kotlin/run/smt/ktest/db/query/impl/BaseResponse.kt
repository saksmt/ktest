package run.smt.ktest.db.query.impl

import run.smt.ktest.db.query.Response
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import kotlin.reflect.KClass

/**
 * Response that can be represented as list, map or single pojo
 */
class BaseResponse<out T : Any>(
    klass: KClass<T>,
    jdbc: JdbcTemplate,
    private val listLoader: (JdbcTemplate) -> List<T>,
    private val mapLoader: (JdbcTemplate) -> Map<String, Any>?,
    private val singleLoader: (JdbcTemplate) -> T?
) : Response<T> {

    val asList: List<T> by lazy {
        listLoader(jdbc)
    }

    val asMap: Map<String, Any>? by lazy {
        nullOnEmptyResultDataAccessException(mapLoader)(jdbc)
    }

    @Suppress("UNCHECKED_CAST")
    val single: T? by lazy {
        if (klass == Map::class) {
            mapLoader(jdbc) as T?
        } else {
            nullOnEmptyResultDataAccessException(singleLoader)(jdbc)
        }
    }

    override fun asList(): List<T> {
        return asList
    }

    override fun asMap(): Map<String, Any>? {
        return asMap
    }

    override fun single(): T? {
        return single
    }

    private fun <R> nullOnEmptyResultDataAccessException(f: (JdbcTemplate) -> R?): (JdbcTemplate) -> R? {
        return {
            try {
                f(it)
            } catch (e: EmptyResultDataAccessException) {
                null
            }
        }
    }
}
