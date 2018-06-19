package run.smt.ktest.rest.impl

import com.typesafe.config.Config
import io.restassured.filter.Filter
import run.smt.ktest.config.get
import run.smt.ktest.rest.api.RequestBuilder
import run.smt.ktest.rest.api.RestContext
import run.smt.ktest.rest.authorization.AuthorizationAdapter
import run.smt.ktest.util.functional.Try.fold
import run.smt.ktest.util.loader.load

class RestContextImpl(private val config: Config) : RestContext {
    private val baseUrl: String = config["base-url"]
    private val socketTimeout: Int = config["socketTimeout"]
    private val connectTimeout: Int = config["connectTimeout"]

    private val authorizationAdapter: AuthorizationAdapter by lazy(this::createAuthorizationAdapter)
    private val logger: Filter by lazy { getLogger(config["logger"]) }

    private fun createAuthorizationAdapter(): AuthorizationAdapter {
        val adapterName: String = config["authorization.adapter"]
        val adapterClassName: String = if (config.hasPath("authorization.adapters.$adapterName")) {
            config["authorization.adapters.$adapterName"]
        } else {
            adapterName
        }
        val result = load<AuthorizationAdapter>(adapterClassName)
            .fold { throw it }
        result.setup(config) // todo: replace with constructor injection
        return result
    }

    override operator fun <T> invoke(action: RequestBuilder.() -> T): T {
        return RestAssuredRequestsAdapter(baseUrl, authorizationAdapter, logger, socketTimeout, connectTimeout).action()
    }
}
