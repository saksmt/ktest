package run.smt.ktest.resttest.api

import com.typesafe.config.Config
import run.smt.ktest.BaseSpec
import run.smt.ktest.rest.api.RestContext
import run.smt.ktest.rest.rest
import run.smt.ktest.rest.url.UrlDsl
import run.smt.ktest.rest.url.UrlProvider
import run.smt.ktest.rest.url.createUrlDsl
import run.smt.ktest.resttest.skeleton.defaultSkeletons
import kotlin.reflect.KClass

typealias RestTestParamsDSL<U> = RestTestParams<U>.() -> Unit

/**
 * DSL for configuring rest-test URLs source
 */
data class RestTestParams<T : UrlProvider> internal constructor(
    var urlDsl: UrlDsl<T>? = null,
    var restDsl: RestContext = rest,
    var skeletons: Map<KClass<out BaseSpec>, RestTestSpecSkeleton<*>> = defaultSkeletons
) {
    fun urlDsl(provider: T) {
        urlDsl = createUrlDsl(provider)
    }

    fun urlDsl(providerFactory: (Config) -> T) {
        urlDsl = createUrlDsl(providerFactory)
    }

    fun restDsl(dslSource: RestContext) {
        restDsl = dslSource
    }
}

fun <U : UrlProvider> configureRestTest(builder: RestTestParamsDSL<U>) = RestTestParams<U>().apply(builder)
