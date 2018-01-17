package run.smt.ktest.specs

import run.smt.ktest.allure.*
import run.smt.ktest.api.BaseSpec
import run.smt.ktest.api.MetaInfoDSL
import run.smt.ktest.api.internal.SpecBuilder
import run.smt.ktest.api.plus

abstract class AllureSpec(init: AllureSpec.() -> Unit) : BaseSpec() {
    init {
        this.init()
    }

    fun <T> epic(name: String, body: () -> T) = epic(name, {}, body)
    fun <T> epic(name: String, metaInfo: MetaInfoDSL, body: () -> T) = SpecBuilder.suite(
        "Epic: $name",
        metaInfo + { epic(name) },
        body
    )

    fun <T> feature(name: String, body: () -> T) = feature(name, {}, body)
    fun <T> feature(name: String, metaInfo: MetaInfoDSL, body: () -> T) = SpecBuilder.suite(
        "Feature: $name",
        metaInfo + { feature(name) },
        body
    )

    fun <T> story(name: String, body: () -> T) = story(name, {}, body)
    fun <T> story(name: String, metaInfo: MetaInfoDSL, body: () -> T) = SpecBuilder.suite(
        "Story: $name",
        metaInfo + { story(name) },
        body
    )

    fun case(name: String, body: () -> Unit) = case(name, {}, body)
    fun case(name: String, metaInfo: MetaInfoDSL, body: () -> Unit) = SpecBuilder.case(
        "Test Case: $name",
        metaInfo + { title(name) },
        body
    )
}
