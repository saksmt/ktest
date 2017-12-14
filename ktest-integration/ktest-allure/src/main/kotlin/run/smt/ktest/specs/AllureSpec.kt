package run.smt.ktest.specs

import org.junit.runner.RunWith
import run.smt.ktest.BaseSpec
import run.smt.ktest.allure.AllureMetaInfoDSL
import run.smt.ktest.internal.KTestJUnitRunner
import run.smt.ktest.allure.metaInfo as _metaInfo
import run.smt.ktest.internal.api.SpecBuilder

@RunWith(KTestJUnitRunner::class)
abstract class AllureSpec(init: AllureSpec.() -> Unit) : BaseSpec() {
    init {
        this.init()
    }

    companion object {
        fun metaInfo(dsl: AllureMetaInfoDSL) = _metaInfo(dsl)
    }

    fun <T> epic(name: String, vararg annotations: Annotation = emptyArray(), body: () -> T) = epic(name, annotations.toList(), body)
    fun <T> epic(name: String, annotations: List<Annotation> = emptyList(), body: () -> T) = SpecBuilder.suite(
        "Epic: $name",
        metaInfo { epic(name) } + annotations,
        body
    )

    fun <T> feature(name: String, vararg annotations: Annotation = emptyArray(), body: () -> T) = feature(name, annotations.toList(), body)
    fun <T> feature(name: String, annotations: List<Annotation> = emptyList(), body: () -> T) = SpecBuilder.suite(
        "Feature: $name",
        metaInfo { feature(name) } + annotations,
        body
    )

    fun <T> story(name: String, vararg annotations: Annotation = emptyArray(), body: () -> T) = story(name, annotations.toList(), body)
    fun <T> story(name: String, annotations: List<Annotation> = emptyList(), body: () -> T) = SpecBuilder.suite(
        "Story: $name",
        metaInfo { story(name) } + annotations,
        body
    )

    fun case(name: String, vararg annotations: Annotation = emptyArray(), body: () -> Unit) = case(name, annotations.toList(), body)
    fun case(name: String, annotations: List<Annotation> = emptyList(), body: () -> Unit) = SpecBuilder.case(
        "Test Case: $name",
        metaInfo { title(name) } + annotations,
        body
    )
}
