package at.meks.pv.forecast.battery.calculation

import at.meks.pv.forecast.battery.calculation.model.PowerData
import at.meks.pv.forecast.battery.calculation.model.SinglePowerData
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class OriginalPowerDataAggregationTest {

    @Test
    fun fedInPerMonthAsString() {
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
        assertEquals("-1.1, -2.4, -3.4, -4.4, -5.4, -6.4, -0.0, -7.4, -8.4, -9.4, -10.4, -11.4", dataIntegration.fedInPerMonthAsString())
    }
}