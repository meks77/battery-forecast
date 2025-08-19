package at.meks.pv.forecast.battery.powerdata

import at.meks.pv.forecast.battery.Year
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertContentEquals

class PowerDataRepoTest {

    val repo = PowerDataRepo()


    @Test
    fun yearsOnlyYearsContainingPowerDataAreReturned() {
        repo.add(SinglePowerData(LocalDateTime(2022, 12, 31, 23, 59), 1.0, 0.0))
        repo.add(SinglePowerData(LocalDateTime(2023, 1, 1, 0, 0), 1.0, 0.0))
        repo.add(SinglePowerData(LocalDateTime(2023, 1, 1, 0, 1), 1.0, 0.0))
        repo.add(SinglePowerData(LocalDateTime(2023, 1, 1, 0, 2), 1.0, 0.0))
        repo.add(SinglePowerData(LocalDateTime(2024, 1, 1, 0, 0), 1.0, 0.0))

        val result = repo.years()

        assertContentEquals(listOf(2022, 2023), result)
    }


    @Test
    fun powerDataOnlyDataContainingPowerDataWithinTheYearAreReturned() {
        repo.add(SinglePowerData(LocalDateTime(2022, 12, 31, 23, 59), 1.0, 0.0))
        repo.add(SinglePowerData(LocalDateTime(2023, 1, 1, 0, 0), 1.0, 0.0))
        repo.add(SinglePowerData(LocalDateTime(2023, 1, 1, 0, 1), 1.0, 0.0))
        repo.add(SinglePowerData(LocalDateTime(2023, 12, 31, 23, 59, 59), 1.0, 0.0))
        repo.add(SinglePowerData(LocalDateTime(2024, 1, 1, 0, 0, 0), 1.0, 0.0))
        repo.add(SinglePowerData(LocalDateTime(2024, 1, 1, 0, 1, 0), 1.0, 0.0))

        val result = repo.powerData(Year(2023)).getPowerData()

        assertContentEquals(
            listOf(
                SinglePowerData(LocalDateTime(2023, 1, 1, 0, 1), 1.0, 0.0),
                SinglePowerData(LocalDateTime(2023, 12, 31, 23, 59, 59), 1.0, 0.0),
                SinglePowerData(LocalDateTime(2024, 1, 1, 0, 0), 1.0, 0.0)
            ),
            result)

    }


}