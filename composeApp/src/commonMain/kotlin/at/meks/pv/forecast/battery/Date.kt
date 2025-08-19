package at.meks.pv.forecast.battery

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun LocalDateTime.minusSeconds(amount: Int): LocalDateTime {
    return this.toInstant(TimeZone.currentSystemDefault()).minus(amount, DateTimeUnit.SECOND)
        .toLocalDateTime(TimeZone.currentSystemDefault())
}

data class Year(private val value:Int) {

    fun getValue() = value

    companion object {
        @OptIn(ExperimentalTime::class)
        fun now(): Year {
            return Year(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year)
        }

    }
}