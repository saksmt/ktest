package run.smt.ktest.rest.impl

import com.typesafe.config.Config
import run.smt.ktest.config.fallbackTo
import run.smt.ktest.config.get
import run.smt.ktest.rest.api.RequestBuilder
import run.smt.ktest.rest.api.RestContext
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class RestContextRegistry(private val config: Config) : RestContext {
    private val registered: ConcurrentMap<String, RestContext> = ConcurrentHashMap()

    override operator fun <T> invoke(action: RequestBuilder.() -> T): T = this["rest"](action)

    operator fun get(configName: String): RestContext = registered.computeIfAbsent(configName, this::createContext)

    private fun createContext(configName: String): RestContext {
        if (config.hasPath(configName)) {
            val instanceConfig: Config = config[configName]
            val defaultConfig: Config = config["__DEFAULTS__.rest"]
            return RestContextImpl(instanceConfig fallbackTo defaultConfig)
        }
        throw IllegalArgumentException()
    }
}
