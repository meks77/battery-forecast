package at.meks.calculation

import at.meks.powerdata.PowerData
import at.meks.powerdata.SinglePowerData
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class OriginalPowerDataAggregationTest {

    @Test
    fun fedInPerMonthAsString() {
        val powerData = PowerData(mutableListOf(
            SinglePowerData(LocalDateTime.of(2024, 1, 2, 7,15), 1.1, 94.0),
            SinglePowerData(LocalDateTime.of(2025, 2, 4, 0, 15), 2.4, 94.0),
            SinglePowerData(LocalDateTime.of(2025, 3, 4, 0, 15), 3.4, 94.0),
            SinglePowerData(LocalDateTime.of(2025, 4, 4, 0, 15), 4.4, 94.0),
            SinglePowerData(LocalDateTime.of(2025, 5, 4, 0, 15), 5.4, 94.0),
            SinglePowerData(LocalDateTime.of(2025, 6, 4, 0, 15), 6.4, 94.0),
            SinglePowerData(LocalDateTime.of(2025, 8, 4, 0, 15), 7.4, 94.0),
            SinglePowerData(LocalDateTime.of(2025, 9, 4, 0, 15), 8.4, 94.0),
            SinglePowerData(LocalDateTime.of(2025, 10, 4, 0, 15), 9.4, 94.0),
            SinglePowerData(LocalDateTime.of(2025, 11, 4, 0, 15), 10.4, 94.0),
            SinglePowerData(LocalDateTime.of(2025, 12, 4, 0, 15), 11.4, 94.0),
            ))
        val dataIntegration = OriginalPowerDataAggregation(powerData, listOf(2024))
        assertThat(dataIntegration.fedInPerMonthAsString())
            .isEqualTo("-1.1, -2.4, -3.4, -4.4, -5.4, -6.4, -0.0, -7.4, -8.4, -9.4, -10.4, -11.4")
    }
}