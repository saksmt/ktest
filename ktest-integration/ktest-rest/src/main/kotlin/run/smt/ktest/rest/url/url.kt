package run.smt.ktest.rest.url

import com.typesafe.config.Config
import run.smt.ktest.config.config
import run.smt.ktest.config.get
import run.smt.ktest.config.fallbackTo

typealias UrlDsl<U> = (U.() -> String) -> String

/**
 * Create DSL for URLs based on constructor for UrlProvider
 */
fun <T : UrlProvider> createUrlDsl(providerFactory: (Config) -> T): UrlDsl<T> {
    val urls: Config = config["urls"]
    val url: Config = config["url"]
    return createUrlDsl(providerFactory(urls fallbackTo url))
}

/**
 * Create DSL for URLs based on UrlProvider
 */
fun <T : UrlProvider> createUrlDsl(provider: T): UrlDsl<T> = provider::run

/**
 * Source of URLs
 */
interface UrlProvider {
    fun param(name: String) = "{$name}"
    operator fun String.div(other: String) = this + "/" + other
}
