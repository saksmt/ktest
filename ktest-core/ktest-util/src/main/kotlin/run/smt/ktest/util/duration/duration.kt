package run.smt.ktest.util.duration

import java.time.Duration
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit

/**
 * Usage example: ```

val testTime = inSeconds { 10.days() + 2.hours() + 3.minutes() + 15.seconds() + 2.millis() }

println(inSeconds { 2.minutes() })
println(inDays { 240.hours() })
println(inMillis { testTime.seconds().show() })
println(inSeconds { testTime.seconds().show() })
println(inMinutes { testTime.seconds().show() })
println(inHours { testTime.seconds().show() })
println(inDays { testTime.seconds().show() })

 * ```
 */

val infinity = Long.MAX_VALUE
val infiniteDuration = inNanos { infinity.nanos().toDuration() }

abstract class TimeUnit<T : TimeUnit<T>> {
    companion object {
        data class PrettyText(
            val text: String,
            val remainder: Long
        )
    }

    operator fun <R> invoke(contextAware: T.() -> R): R = self().contextAware()

    abstract fun unitName(): String
    abstract fun shortUnitName(): String
    abstract fun temporalUnit(): TemporalUnit

    abstract fun Int.nanos(): Int
    abstract fun Int.millis(): Int
    abstract fun Int.seconds(): Int
    abstract fun Int.minutes(): Int
    abstract fun Int.hours(): Int
    abstract fun Int.days(): Int

    abstract fun Long.nanos(): Long
    abstract fun Long.millis(): Long
    abstract fun Long.seconds(): Long
    abstract fun Long.minutes(): Long
    abstract fun Long.hours(): Long
    abstract fun Long.days(): Long

    fun Long.toDuration(): Duration = Duration.of(this, temporalUnit())
    fun Int.toDuration(): Duration = toLong().toDuration()

    protected abstract fun self(): T
    protected abstract fun <T : Number> internalShow(numberToShow: T): PrettyText

    protected inline fun <T : Number, P : TimeUnit<P>> parametrizedShow(
        greaterContext: P,
        crossinline selfUnit: P.(Long) -> Long,
        crossinline greaterUnit: Long.() -> Long
    ): (T) -> PrettyText = { numberToShow ->
        val n = numberToShow.toLong()
        val greaterValue = greaterContext { selfUnit(n) }
        if (greaterValue == 0L) {
            PrettyText("", numberToShow.toLong())
        } else {
            PrettyText(greaterContext { greaterValue.show() }, n - greaterValue.greaterUnit())
        }
    }

    fun <T : Number> T.show(): String {
        val internal = internalShow(this)
        val selfValue = "${internal.remainder}${shortUnitName()}"
        if (internal.text.isBlank()) {
            return selfValue
        }
        return "${internal.text} $selfValue"
    }
}

inline fun <T> inNanos(computation: NanosBase.() -> T): T = NanosBase.computation()

object NanosBase : TimeUnit<NanosBase>() {
    override fun self() = this

    override fun Int.nanos() = this
    override fun Int.millis() = 1000000 * nanos()
    override fun Int.seconds() = 1000 * millis()
    override fun Int.minutes() = 60 * seconds()
    override fun Int.hours() = 60 * minutes()
    override fun Int.days() = 24 * hours()

    override fun Long.nanos() = this
    override fun Long.millis() = 1000000L * nanos()
    override fun Long.seconds() = 1000L * millis()
    override fun Long.minutes() = 60L * seconds()
    override fun Long.hours() = 60L * minutes()
    override fun Long.days() = 24L * hours()

    override fun unitName(): String = "nanosecond"

    override fun shortUnitName(): String = "n"

    override fun temporalUnit(): TemporalUnit = ChronoUnit.NANOS

    override fun <T : Number> internalShow(numberToShow: T): PrettyText {
        return (NanosBase.parametrizedShow<T, MillisBase>(
            MillisBase,
            { it.nanos() },
            { millis() }
        ))(numberToShow)
    }
}

inline fun <T> inMillis(computation: MillisBase.() -> T): T = MillisBase.computation()

object MillisBase : TimeUnit<MillisBase>() {
    override fun self() = this

    override fun Int.nanos() = millis() / 1000000
    override fun Int.millis() = this
    override fun Int.seconds() = 1000 * millis()
    override fun Int.minutes() = 60 * seconds()
    override fun Int.hours() = 60 * minutes()
    override fun Int.days() = 24 * hours()

    override fun Long.nanos() = millis() / 1000000
    override fun Long.millis() = this
    override fun Long.seconds() = 1000L * millis()
    override fun Long.minutes() = 60L * seconds()
    override fun Long.hours() = 60L * minutes()
    override fun Long.days() = 24L * hours()

    override fun unitName(): String = "millisecond"

    override fun shortUnitName(): String = "ms"

    override fun temporalUnit(): TemporalUnit = ChronoUnit.MILLIS

    override fun <T : Number> internalShow(numberToShow: T): PrettyText {
        return parametrizedShow<T, SecondsBase>(
            SecondsBase,
            { it.millis() },
            { seconds() }
        )(numberToShow)
    }
}

inline fun <T> inSeconds(computation: SecondsBase.() -> T): T = SecondsBase.computation()

object SecondsBase : TimeUnit<SecondsBase>() {
    override fun self() = this

    override fun Int.nanos() = millis() / 1000000
    override fun Int.millis() = seconds() / 1000
    override fun Int.seconds() = this
    override fun Int.minutes() = 60 * seconds()
    override fun Int.hours() = 60 * minutes()
    override fun Int.days() = 24 * hours()

    override fun Long.nanos() = millis() / 1000000
    override fun Long.millis() = seconds() / 1000
    override fun Long.seconds() = this
    override fun Long.minutes() = 60L * seconds()
    override fun Long.hours() = 60L * minutes()
    override fun Long.days() = 24L * hours()

    override fun unitName(): String = "second"

    override fun shortUnitName(): String = "s"

    override fun temporalUnit(): TemporalUnit = ChronoUnit.SECONDS

    override fun <T : Number> internalShow(numberToShow: T): PrettyText {
        return parametrizedShow<T, MinutesBase>(
            MinutesBase,
            { it.seconds() },
            { minutes() }
        )(numberToShow)
    }
}

inline fun <T> inMinutes(computation: MinutesBase.() -> T): T = MinutesBase.computation()

object MinutesBase : TimeUnit<MinutesBase>() {
    override fun self() = this

    override fun Int.nanos() = millis() / 1000000
    override fun Int.millis() = seconds() / 1000
    override fun Int.seconds() = minutes() / 60
    override fun Int.minutes() = this
    override fun Int.hours() = 60 * minutes()
    override fun Int.days() = 24 * hours()

    override fun Long.nanos() = millis() / 1000000
    override fun Long.millis() = seconds() / 1000
    override fun Long.seconds() = minutes() / 60
    override fun Long.minutes() = this
    override fun Long.hours() = 60 * minutes()
    override fun Long.days() = 24 * hours()

    override fun unitName(): String = "minute"

    override fun shortUnitName(): String = "m"

    override fun temporalUnit(): TemporalUnit = ChronoUnit.MINUTES

    override fun <T : Number> internalShow(numberToShow: T): PrettyText {
        return parametrizedShow<T, HoursBase>(
            HoursBase,
            { it.minutes() },
            { hours() }
        )(numberToShow)
    }
}

inline fun <T> inHours(computation: HoursBase.() -> T): T = HoursBase.computation()

object HoursBase : TimeUnit<HoursBase>() {
    override fun self() = this

    override fun Int.nanos() = millis() / 1000000
    override fun Int.millis() = seconds() / 1000
    override fun Int.seconds() = minutes() / 60
    override fun Int.minutes() = hours() / 60
    override fun Int.hours() = this
    override fun Int.days() = 24 * hours()

    override fun Long.nanos() = millis() / 1000000
    override fun Long.millis() = seconds() / 1000
    override fun Long.seconds() = minutes() / 60
    override fun Long.minutes() = hours() / 60
    override fun Long.hours() = this
    override fun Long.days() = 24 * hours()

    override fun unitName(): String = "hour"

    override fun shortUnitName(): String = "h"

    override fun temporalUnit(): TemporalUnit = ChronoUnit.HOURS

    override fun <T : Number> internalShow(numberToShow: T): PrettyText {
        return parametrizedShow<T, DaysBase>(
            DaysBase,
            { it.hours() },
            { days() }
        )(numberToShow)
    }
}

inline fun <T> inDays(computation: DaysBase.() -> T): T = DaysBase.computation()

object DaysBase : TimeUnit<DaysBase>() {
    override fun self() = this

    override fun Int.nanos() = millis() / 1000000
    override fun Int.millis() = seconds() / 1000
    override fun Int.seconds() = minutes() / 60
    override fun Int.minutes() = hours() / 60
    override fun Int.hours() = days() / 24
    override fun Int.days() = this

    override fun Long.nanos() = millis() / 1000000
    override fun Long.millis() = seconds() / 1000
    override fun Long.seconds() = minutes() / 60
    override fun Long.minutes() = hours() / 60
    override fun Long.hours() = days() / 24
    override fun Long.days() = this

    override fun unitName(): String = "day"

    override fun shortUnitName(): String = "D"

    override fun temporalUnit(): TemporalUnit = ChronoUnit.DAYS

    override fun <T : Number> internalShow(numberToShow: T): PrettyText {
        return PrettyText("", numberToShow.toLong())
    }
}
