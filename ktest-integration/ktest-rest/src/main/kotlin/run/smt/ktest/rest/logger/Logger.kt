package run.smt.ktest.rest.logger

import io.restassured.response.Response
import io.restassured.specification.FilterableRequestSpecification

/**
 * Logs request and response. Implementors MUST have either no constructor arguments
 * or just one argument of type [com.typesafe.config.Config]
 */
interface Logger {
    fun log(request: FilterableRequestSpecification, response: Response)
}
