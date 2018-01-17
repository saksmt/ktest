package run.smt.ktest.allure.model

import io.qameta.allure.SeverityLevel
import io.qameta.allure.model.Label
import io.qameta.allure.model.Link
import io.qameta.allure.util.ResultsUtils.*
import run.smt.ktest.api.MetaProperty

sealed class AllureMetaProperty<out T> : MetaProperty<T>()

sealed class AllureMarker : AllureMetaProperty<Unit>() {
    override val value = Unit
}

sealed class AllureLinkProperty constructor(override val value: Link) : AllureMetaProperty<Link>() {
    constructor(type: String, value: String): this(
        createLink(value, null, null, type)
    )

    constructor(
        type: String,
        name: String,
        url: String
    ) : this(
        createLink(null, type, name, url)
    )
}

class Issue(
    name: String
) : AllureLinkProperty(ISSUE_LINK_TYPE, name)

class TmsLink(
    ticket: String
) : AllureLinkProperty(TMS_LINK_TYPE, ticket)

class OtherLink(
    name: String,
    url: String,
    type: String = "custom"
) : AllureLinkProperty(type, name, url)


sealed class AllureLabelProperty(
    private val name: String,
    private val labelValue: String
) : AllureMetaProperty<Label>() {
    override val value: Label
        get() = Label().withName(name).withValue(labelValue)
}

class EpicLabel(name: String) : AllureLabelProperty(
    EPIC_LABEL_NAME,
    name
)

class FeatureLabel(name: String) : AllureLabelProperty(
    FEATURE_LABEL_NAME,
    name
)

class StoryLabel(name: String) : AllureLabelProperty(
    STORY_LABEL_NAME,
    name
)

class SeverityLabel(
    severityLevel: SeverityLevel
) : AllureLabelProperty(
    SEVERITY_LABEL_NAME,
    severityLevel.value()
)

class TagLabel(
    name: String
) : AllureLabelProperty(
    TAG_LABEL_NAME,
    name
)

class OwnerLabel(
    name: String
) : AllureLabelProperty(
    OWNER_LABEL_NAME,
    name
)

data class Description(
    override val value: String
) : AllureMetaProperty<String>()

data class DisplayName(
    override val value: String
) : AllureMetaProperty<String>()

object Muted : AllureMarker()
object Flaky : AllureMarker()
