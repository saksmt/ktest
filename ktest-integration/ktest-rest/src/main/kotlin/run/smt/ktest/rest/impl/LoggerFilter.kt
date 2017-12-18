package run.smt.ktest.rest.impl

import com.typesafe.config.Config
import io.restassured.filter.Filter
import io.restassured.filter.FilterContext
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification
import io.restassured.specification.FilterableResponseSpecification
import org.slf4j.LoggerFactory
import run.smt.ktest.rest.logger.Logger
import run.smt.ktest.rest.logger.LoggerInstantiator
import java.util.concurrent.ConcurrentHashMap

/**
 * Log filter for rest assured - writes logs to given StringBuilder
 */
internal class LoggerFilter(private val logger: Logger) : Filter {

    companion object {
        private val log = LoggerFactory.getLogger(LoggerFilter::class.java)
    }

    override fun filter(requestSpec: FilterableRequestSpecification, responseSpec: FilterableResponseSpecification, ctx: FilterContext): Response {
        val response = ctx.next(requestSpec, responseSpec)

        try {
            logger.log(requestSpec, response)
        } catch (e: Exception) {
            log.error("REST logger failed due to: ", e)
        }

        return response
    }
}

private fun createLogger(config: Config) = LoggerFilter(LoggerInstantiator(config).instantiate())
private val registeredLoggers = ConcurrentHashMap<Config, Filter>()

internal fun getLogger(config: Config) = registeredLoggers.computeIfAbsent(config, ::createLogger)
