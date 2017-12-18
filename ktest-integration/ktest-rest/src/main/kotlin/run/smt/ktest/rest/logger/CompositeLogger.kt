package run.smt.ktest.rest.logger

import com.typesafe.config.Config
import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification

class CompositeLogger(config: Config) : Logger {
    private val delegates: List<Logger> by lazy {
        config.getObjectList("loggers").map { LoggerInstantiator(it.toConfig()).instantiate() }
    }
    override fun log(request: FilterableRequestSpecification, response: Response) {
        delegates.forEach { it.log(request, response) }
    }
}
