package run.smt.ktest.allure

import io.qameta.allure.*
import run.smt.ktest.allure.model.*
import run.smt.ktest.allure.model.Description
import run.smt.ktest.allure.model.Flaky
import run.smt.ktest.allure.model.Issue
import run.smt.ktest.allure.model.Muted
import run.smt.ktest.allure.model.TmsLink
import run.smt.ktest.api.MetaInfoBuilder
import run.smt.ktest.api.internal.Internals
import java.lang.annotation.Repeatable as JRepeatable

val MetaInfoBuilder.blocker
    get() = SeverityLevel.BLOCKER
val MetaInfoBuilder.critical
    get() = SeverityLevel.CRITICAL
val MetaInfoBuilder.normal
    get() = SeverityLevel.NORMAL
val MetaInfoBuilder.minor
    get() = SeverityLevel.MINOR
val MetaInfoBuilder.trivial
    get() = SeverityLevel.TRIVIAL

fun MetaInfoBuilder.testCaseId(id: String) {
    Internals.register(TmsLink(id))
}

fun MetaInfoBuilder.feature(name: String) {
    Internals.register(FeatureLabel(name))
}

fun MetaInfoBuilder.story(name: String) {
    Internals.register(StoryLabel(name))
}

fun MetaInfoBuilder.epic(name: String) {
    Internals.register(EpicLabel(name))
}

fun MetaInfoBuilder.description(description: String) {
    Internals.register(Description(description))
}

fun MetaInfoBuilder.title(name: String) {
    Internals.register(DisplayName(name))
}

fun MetaInfoBuilder.attachement(resourceName: String, type: String = "", extension: String = "") {
    val normalizedExtension = extension.trim()
    a<Attachment>(
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

/**
 * Use this annotation to add some links to results
 *
 * @param name Name for link
 * @param url Url for link. By default will search for system property `allure.link.{type}.pattern`, and use it
 * to generate url.
 * @param type This type is used for create an icon for link. Also there is few reserved types such as issue and tms.
 */
fun MetaInfoBuilder.link(name: String, url: String = "", type: String = "custom") {
    Internals.register(OtherLink(
        name,
        url,
        type
    ))
}

fun MetaInfoBuilder.blocker() = severity(level = blocker)
fun MetaInfoBuilder.critical() = severity(level = critical)
fun MetaInfoBuilder.normal() = severity(level = normal)
fun MetaInfoBuilder.minor() = severity(level = minor)
fun MetaInfoBuilder.trivial() = severity(level = trivial)

fun MetaInfoBuilder.severity(level: SeverityLevel) {
    Internals.register(SeverityLabel(level))
}

fun MetaInfoBuilder.issue(name: String) {
    Internals.register(Issue(name))
}

fun MetaInfoBuilder.muted() {
    Internals.register(Muted)
}

fun MetaInfoBuilder.owner(name: String) {
    Internals.register(OwnerLabel(name))
}

fun MetaInfoBuilder.flaky() {
    Internals.register(Flaky)
}
