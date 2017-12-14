package run.smt.ktest.allure

import io.qameta.allure.*
import io.qameta.allure.junit4.DisplayName
import io.qameta.allure.junit4.Tag
import org.junit.Ignore
import run.smt.ktest.util.reflection.a
import java.lang.annotation.Repeatable as JRepeatable
import run.smt.ktest.util.collection.*

typealias AllureMetaInfoDSL = AllureMetaInfoBuilder.() -> Unit
class AllureMetaInfoBuilder internal constructor() {
    val blocker = SeverityLevel.BLOCKER
    val critical = SeverityLevel.CRITICAL
    val normal = SeverityLevel.NORMAL
    val minor = SeverityLevel.MINOR
    val trivial = SeverityLevel.TRIVIAL

    internal val annotations = mutableListOf<Annotation>()

    fun testCaseId(id: String) {
        annotations += a<TmsLink>(id)
    }

    fun feature(name: String) {
        annotations += a<Feature>(name)
    }

    fun story(name: String) {
        annotations += a<Story>(name)
    }

    fun epic(name: String) {
        annotations += a<Epic>(name)
    }

    /**
     * Allure's display name from junit4 integration layer
     */
    fun title(name: String) {
        annotations += a<DisplayName>(name)
    }

    fun description(description: String) {
        annotations += a<Description>(description)
    }

    fun attachement(resourceName: String, type: String = "", extension: String = "") {
        val normalizedExtension = extension.trim()
        annotations += a<Attachment>(
            "value" to resourceName,
            "type" to type,
            "fileExtension" to
                if (normalizedExtension.startsWith(".") || normalizedExtension.isEmpty()) {
                    extension
                } else {
                    ".$extension"
                }
        )
    }

    fun disabled(disabled: Boolean = true) {
        if (disabled) {
            annotations += a<Ignore>()
        }
    }

    fun disabled(because: String) {
        annotations += a<Ignore>(because)
    }

    /**
     * Use this annotation to add some links to results
     *
     * @param name Name for link
     * @param url Url for link. By default will search for system property `allure.link.{type}.pattern`, and use it
     * to generate url.
     * @param type This type is used for create an icon for link. Also there is few reserved types such as issue and tms.
     */
    fun link(name: String, url: String = "", type: String = "custom") {
        annotations += a<Link>(
            "name" to name,
            "url" to url,
            "type" to type
        )
    }

    fun blocker() = severity(level = blocker)
    fun critical() = severity(level = critical)
    fun normal() = severity(level = normal)
    fun minor() = severity(level = minor)
    fun trivial() = severity(level = trivial)

    fun severity(level: SeverityLevel) {
        annotations += a<Severity>(level)
    }

    fun issue(name: String) {
        annotations += a<Issue>(name)
    }

    fun muted() {
        annotations += a<Muted>()
    }

    fun owner(name: String) {
        annotations += a<Owner>(name)
    }

    fun flaky() {
        annotations += a<Flaky>()
    }

    fun tag(name: String) {
        annotations += a<Tag>(name)
    }
}

internal fun normalize(annotations: List<Annotation>): List<Annotation> {
    val (normalizable, alreadyNormalized) = annotations.asSequence()
        .groupBy { it::class }
        .toList()
        .partitionBy { it.first.annotations.any { it is JRepeatable } && it.second.size > 1 }

    return alreadyNormalized.flatMap { it.second } +
        normalizable.map { (k, v) ->
            val wrapper = k.annotations.find { it is JRepeatable } as? JRepeatable
                ?: throw IllegalStateException("Can't really happen")
            a(wrapper.value, v.toTypedArray())
        }
}

fun metaInfo(builder: AllureMetaInfoBuilder) = normalize(builder.annotations)
fun metaInfo(dsl: AllureMetaInfoDSL) = metaInfo(AllureMetaInfoBuilder().apply(dsl))
