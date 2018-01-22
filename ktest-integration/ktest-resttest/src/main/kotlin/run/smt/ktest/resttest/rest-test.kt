package run.smt.ktest.resttest

import run.smt.ktest.api.BaseSpec
import run.smt.ktest.rest.url.UrlProvider
import run.smt.ktest.resttest.api.*
import run.smt.ktest.resttest.impl.RootRestTestContext

fun <U : UrlProvider> createRestTestDSL(spec: BaseSpec, params: RestTestParams<U>): RootRestTestContext<U> {
    return RootRestTestContext(params, spec)
}

fun <U : UrlProvider> createRestTestDSL(spec: BaseSpec, paramsDSL: RestTestParamsDSL<U>) = createRestTestDSL(spec, configureRestTest(paramsDSL))

// for easier import we place expectation extensions here

inline fun <reified T : Any> RestTestDefinition<*>.expect(noinline expectation: Expectation<T>) {
    expect(T::class, expectation)
}

inline fun <reified T : Any> RestTestDefinition<*>.expect(noinline expectation: StatusCodeAwareExpectation<T>) {
    expect(T::class, expectation)
}
