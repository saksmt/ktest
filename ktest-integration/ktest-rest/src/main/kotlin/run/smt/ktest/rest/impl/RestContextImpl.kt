package run.smt.ktest.rest.impl

import com.typesafe.config.Config
import io.restassured.filter.Filter
import run.smt.ktest.config.get
import run.smt.ktest.rest.api.RequestBuilder
import run.smt.ktest.rest.api.RestContext
import run.smt.ktest.rest.authorization.AuthorizationAdapter

class RestContextImpl(private val config: Config) : RestContext {
    private val baseUrl: String = config["base-url"]
    private val socketTimeout: Int = config["socket-timeout"]
    private val connectTimeout: Int = config["connect-timeout"]

    private val authorizationAdapter: AuthorizationAdapter by lazy(this::createAuthorizationAdapter)
    private val logger: Filter by lazy { getLogger(config["logger"]) }

    private fun createAuthorizationAdapter(): AuthorizationAdapter {
        val adapterName: String = config["authorization.adapter"]
        @Suppress("UNCHECKED_CAST")
        val adapterClass: Class<AuthorizationAdapter> = Class.forName(
            if (config.hasPath("authorization.adapters.$adapterName")) {
                config["authorization.adapters.$adapterName"]
            } else {
                adapterName
            }
        ) as Class<AuthorizationAdapter>
        val result = adapterClass.newInstance()
        result.setup(config)
        return result
    }

    override operator fun <T> invoke(action: RequestBuilder.() -> T): T {
        return RestAssuredRequestsAdapter(baseUrl, authorizationAdapter, logger, socketTimeout, connectTimeout).action()
    }
}
