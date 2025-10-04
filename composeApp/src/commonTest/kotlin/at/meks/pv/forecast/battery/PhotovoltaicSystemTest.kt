package at.meks.pv.forecast.battery.calculation.model

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals

class PhotovoltaicSystemTest {

    val january = YearMonth(2025, 1)
    val february = YearMonth(2025, 2)
    val march = YearMonth(2025, 3)


    @Test
    fun withinOneMonth() {
        val photovoltaikSystem = PhotovoltaikSystem(Battery(10.0, 6000))
        photovoltaikSystem.add(SinglePowerData(LocalDateTime(2025, 1, 1, 0, 15), 1.0, 0.8))
        assertThatNothingWasFedId(photovoltaikSystem, january)

        photovoltaikSystem.add(SinglePowerData(LocalDateTime(2025, 1, 1, 0, 30), 3.0, 0.8))
        assertThatNothingWasFedId(photovoltaikSystem, january)

        photovoltaikSystem.add(SinglePowerData(LocalDateTime(2025, 1, 1, 0, 45), 8.432, 0.8))
        assertFedIn(photovoltaikSystem, january, 0.832)

        photovoltaikSystem.add(SinglePowerData(LocalDateTime(2025, 1, 1, 1, 0), 10.0, 0.8))
        assertFedIn(photovoltaikSystem, january, 10.032)

        photovoltaikSystem.add(SinglePowerData(LocalDateTime(2025, 1, 2, 0, 0), 10.0, 0.8))
        assertFedIn(photovoltaikSystem, january, 19.232)
    }

    @Test
    fun differentMonths() {
        val photovoltaikSystem = PhotovoltaikSystem(Battery(10.0, 6000))

        photovoltaikSystem.add(SinglePowerData(LocalDateTime(2025, 1, 1, 0, 15), 14.0, 0.8))
        assertFedIn(photovoltaikSystem, january, 4.0)

        photovoltaikSystem.add(SinglePowerData(LocalDateTime(2025, 2, 1, 0, 15), 13.0, 2.8))
        assertFedIn(photovoltaikSystem, february, 13.0 - 2.8)

        photovoltaikSystem.add(SinglePowerData(LocalDateTime(2025, 3, 1, 0, 15), 15.432, 0.72))
        assertFedIn(photovoltaikSystem, march, 15.432 - 0.72)
    }

    private fun assertFedIn(photovoltaikSystem: PhotovoltaikSystem, january: YearMonth, expected: Double) {
        assertEquals(expected, photovoltaikSystem.feedInToGrid()[january]!!, 0.00000001)
    }

    private fun assertThatNothingWasFedId(photovoltaikSystem: PhotovoltaikSystem, month: YearMonth) {
        assertEquals(0.0, photovoltaikSystem.feedInToGrid()[month]!!)
    }
    
}