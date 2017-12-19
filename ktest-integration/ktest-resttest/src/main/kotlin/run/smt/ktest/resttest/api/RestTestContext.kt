package run.smt.ktest.resttest.api

import run.smt.ktest.rest.url.UrlProvider

interface RestTestContext<U : UrlProvider> {
    operator fun invoke(name: (RequestData) -> String = { it.method }, metaInfo: RestMetaInfoBuilder = {}, dsl: RestTestDSL<U>)
    operator fun invoke(name: String, metaInfo: RestMetaInfoBuilder = {}, dsl: RestTestDSL<U>) = this(
        name = { name },
        metaInfo = metaInfo,
        dsl = dsl
    )
}

