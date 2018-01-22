package run.smt.ktest.resttest.impl

import run.smt.ktest.api.BaseSpec
import run.smt.ktest.rest.rest
import run.smt.ktest.rest.url.UrlProvider
import run.smt.ktest.resttest.api.*
import kotlin.reflect.KClass
import kotlin.reflect.full.superclasses

private tailrec fun skeletonFor(clazz: KClass<out BaseSpec>, registered: Map<KClass<out BaseSpec>, RestTestSpecSkeleton<*>>): RestTestSpecSkeleton<BaseSpec>? =
    if (clazz == BaseSpec::class) {
        null
    } else {
        @Suppress("UNCHECKED_CAST")
        registered[clazz] as RestTestSpecSkeleton<BaseSpec>? ?: skeletonFor(clazz.superclasses.first() as KClass<out BaseSpec>, registered)
    }

class RootRestTestContext<U : UrlProvider> internal constructor(
    private val rootParams: RestTestParams<U>,
    private val spec: BaseSpec
) : RestTestContext<U> {

    override fun invoke(name: (RequestData) -> String, metaInfo: RestMetaInfoBuilder, dsl: RestTestDSL<U>) {
        val skel = skeletonFor(spec::class, rootParams.skeletons) ?: throw IllegalStateException(
            "Broken configuration for RESTTest probably because changed skeleton parameter in RestTestParams. " +
                "Did you forget to add default for BaseSpec?"
        )
        skel(spec) {
            RestTestDefinitionImpl(rootParams).apply(dsl).execute(name, metaInfo, it)
        }
    }

    operator fun get(contextName: String): RestTestContext<U> = RootRestTestContext(rootParams.copy(restDsl = rest[contextName]), spec)
}
