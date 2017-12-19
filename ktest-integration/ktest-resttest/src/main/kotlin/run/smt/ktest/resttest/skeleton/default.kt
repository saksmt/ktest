package run.smt.ktest.resttest.skeleton

import run.smt.ktest.BaseSpec
import run.smt.ktest.resttest.api.RestTestSpecSkeleton
import run.smt.ktest.specs.*
import kotlin.reflect.KClass

val defaultSkeletons: Map<KClass<out BaseSpec>, RestTestSpecSkeleton<*>> = mapOf(
    BehaviorSpec::class to BehaviorSpecSkeleton(),
    FeatureSpec::class to FeatureSpecSkeleton(),
    SimpleSpec::class to SimpleSpecSkeleton(),
    AllureSpec::class to AllureSpecSkeleton(),
    FreeSpec::class to FreeSpecSkeleton(),
    WordSpec::class to WordSpecSkeleton(),

    BaseSpec::class to BaseSpecSkeleton()
)
