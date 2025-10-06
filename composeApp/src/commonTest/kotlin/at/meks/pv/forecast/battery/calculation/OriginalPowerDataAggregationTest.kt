package at.meks.pv.forecast.battery.calculation

import at.meks.pv.forecast.battery.calculation.model.PowerData
import at.meks.pv.forecast.battery.calculation.model.SinglePowerData
import io.kotest.matchers.maps.shouldContainExactly
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlin.test.Test

class OriginalPowerDataAggregationTest {

    @Test
    fun fedInPerMonth() {
        val powerData = PowerData(mutableListOf(
            SinglePowerData(LocalDateTime(2024, 1, 2, 7,15), 1.1, 94.0),
            SinglePowerData(LocalDateTime(2025, 2, 4, 0, 15), 2.4, 94.0),
            SinglePowerData(LocalDateTime(2025, 3, 4, 0, 15), 3.4, 94.0),
            SinglePowerData(LocalDateTime(2025, 4, 4, 0, 15), 4.4, 94.0),
            SinglePowerData(LocalDateTime(2025, 5, 4, 0, 15), 5.4, 94.0),
            SinglePowerData(LocalDateTime(2025, 6, 4, 0, 15), 6.4, 94.0),
            SinglePowerData(LocalDateTime(2025, 8, 4, 0, 15), 7.4, 94.0),
            SinglePowerData(LocalDateTime(2025, 9, 4, 0, 15), 8.4, 94.0),
            SinglePowerData(LocalDateTime(2025, 10, 4, 0, 15), 9.4, 94.0),
            SinglePowerData(LocalDateTime(2025, 11, 4, 0, 15), 10.4, 94.0),
            SinglePowerData(LocalDateTime(2025, 12, 4, 0, 15), 11.4, 94.0),
            ))
        val dataIntegration = OriginalPowerDataAggregation(powerData, listOf(2024))

        val result = dataIntegration.fedInPerMonth()
        result.shouldContainExactly(mapOf(
            Pair(Month.JANUARY, -1.1),
            Pair(Month.FEBRUARY, -2.4),
            Pair(Month.MARCH, -3.4),
            Pair(Month.APRIL, -4.4),
            Pair(Month.MAY, -5.4),
            Pair(Month.JUNE, -6.4),
            Pair(Month.JULY, -0.0),
            Pair(Month.AUGUST, -7.4),
            Pair(Month.SEPTEMBER, -8.4),
            Pair(Month.OCTOBER, -9.4),
            Pair(Month.NOVEMBER, -10.4),
            Pair(Month.DECEMBER, -11.4),
        ))
    }
}