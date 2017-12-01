package run.smt.ktest.db.query.impl

import run.smt.ktest.util.reflection.canBeAssignedTo
import java.math.BigDecimal
import java.sql.Date
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Map SQL value to given java one if it is possible
 */
internal infix fun Any.coerceTo(type: Class<*>): Any = when (javaClass) {
    BigDecimal::class.java -> when {
        type.canBeAssignedTo<Long>() -> (this as BigDecimal).toLong()
        type.canBeAssignedTo<Int>() -> (this as BigDecimal).toInt()
        else -> this
    }

    Timestamp::class.java -> when {
        type.canBeAssignedTo<java.util.Date>() -> java.util.Date((this as Timestamp).time)
        type.canBeAssignedTo<Date>() -> Date((this as Timestamp).time)
        type.canBeAssignedTo<LocalDateTime>() -> (this as Timestamp).toLocalDateTime()
        type.canBeAssignedTo<LocalDate>() -> (this as Timestamp).toLocalDateTime().toLocalDate()
        type.canBeAssignedTo<LocalTime>() -> (this as Timestamp).toLocalDateTime().toLocalTime()
        else -> this
    }

    Date::class.java -> when {
        type.canBeAssignedTo<java.util.Date>() -> java.util.Date((this as Date).time)
        type.canBeAssignedTo<LocalDate>() -> (this as Date).toLocalDate()
        else -> this
    }

    else -> this
}
