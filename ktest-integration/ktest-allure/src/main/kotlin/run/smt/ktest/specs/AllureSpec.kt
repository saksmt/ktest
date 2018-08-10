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

    inner class AllurePath internal constructor(val epic: String? = null,
                                                    val feature: String? = null,
                                                    val story: String? = null,
                                                    val case: String? = null) {
        operator fun invoke(metaInfo: MetaInfoDSL = {}, testBody: () -> Unit) {
            fun genSuite(name: String?, suiteDef: (String, () -> Unit) -> Unit, body: () -> Unit) {
                name?.let { suiteDef(it, body) } ?: body()
            }

            genSuite(epic, this@AllureSpec::epic) {
                genSuite(feature, this@AllureSpec::feature) {
                    genSuite(story, this@AllureSpec::story) {
                        case?.let { case(it, metaInfo, testBody) } ?: case("test", metaInfo, testBody)
                    }
                }
            }
        }

        operator fun plus(other: AllurePath) = AllurePath(
            epic = other.epic ?: epic,
            feature = other.feature ?: feature,
            story = other.story ?: story,
            case = other.case ?: case
        )
    }

    fun epic(name: String): AllurePath = AllurePath(epic = name)
    fun feature(name: String): AllurePath = AllurePath(story = name)
    fun story(name: String): AllurePath = AllurePath(story = name)
    fun case(name: String): AllurePath = AllurePath(case = name)

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
