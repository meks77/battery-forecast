package at.meks.pv.forecast.battery.powerdata

import at.meks.pv.forecast.battery.PowerDataRepo
import at.meks.pv.forecast.battery.Year
import at.meks.pv.forecast.battery.adapter.InMemoryPowerDataRepo
import at.meks.pv.forecast.battery.calculation.model.SinglePowerData
import io.kotest.matchers.maps.shouldBeEmpty
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertContentEquals

class PowerDataRepoTest {

    val repo = InMemoryPowerDataRepo()

    @Test
    fun yearsOnlyYearsContainingPowerDataAreReturned() {
        repo.addOrReplace(LocalDateTime(2022, 12, 31, 23, 59), 1.0,
            PowerDataRepo.PowerType.FED_IN)
        repo.addOrReplace(LocalDateTime(2023, 1, 1, 0, 0), 1.0,
            PowerDataRepo.PowerType.FED_IN)
        repo.addOrReplace(LocalDateTime(2023, 1, 1, 0, 1), 1.0,
            PowerDataRepo.PowerType.FED_IN)
        repo.addOrReplace(LocalDateTime(2023, 1, 1, 0, 2), 1.0,
            PowerDataRepo.PowerType.FED_IN)
        repo.addOrReplace(LocalDateTime(2024, 1, 1, 0, 0), 1.0,
            PowerDataRepo.PowerType.FED_IN)

        val result = repo.years()

        assertContentEquals(listOf(2022, 2023), result)
    }


    @Test
    fun powerDataOnlyDataContainingPowerDataWithinTheYearAreReturned() {
        repo.addOrReplace(LocalDateTime(2022, 12, 31, 23, 59), 1.0,
            PowerDataRepo.PowerType.FED_IN)
        repo.addOrReplace(LocalDateTime(2023, 1, 1, 0, 0), 1.0,
            PowerDataRepo.PowerType.FED_IN)
        repo.addOrReplace(LocalDateTime(2023, 1, 1, 0, 1), 1.0,
            PowerDataRepo.PowerType.FED_IN)
        repo.addOrReplace(LocalDateTime(2023, 12, 31, 23, 59, 59), 1.0,
            PowerDataRepo.PowerType.FED_IN)
        repo.addOrReplace(LocalDateTime(2024, 1, 1, 0, 0, 0), 1.0,
            PowerDataRepo.PowerType.FED_IN)
        repo.addOrReplace(LocalDateTime(2024, 1, 1, 0, 1, 0), 1.0,
            PowerDataRepo.PowerType.FED_IN)

        val result = repo.powerData(Year(2023)).getPowerData()

        assertContentEquals(
            listOf(
                SinglePowerData(LocalDateTime(2023, 1, 1, 0, 1), 1.0, 0.0),
                SinglePowerData(LocalDateTime(2023, 12, 31, 23, 59, 59), 1.0, 0.0),
                SinglePowerData(LocalDateTime(2024, 1, 1, 0, 0), 1.0, 0.0)
            ),
            result)

    }

    @Test
    fun deleteAll() {
        repo.addOrReplace(LocalDateTime(2022, 12, 31, 23, 59), 1.0,
            PowerDataRepo.PowerType.FED_IN)
        repo.addOrReplace(LocalDateTime(2023, 1, 1, 0, 0), 1.0,
            PowerDataRepo.PowerType.FED_IN)
        repo.addOrReplace(LocalDateTime(2023, 1, 1, 0, 1), 1.0,
            PowerDataRepo.PowerType.FED_IN)
        repo.addOrReplace(LocalDateTime(2023, 12, 31, 23, 59, 59), 1.0,
            PowerDataRepo.PowerType.FED_IN)
        repo.addOrReplace(LocalDateTime(2024, 1, 1, 0, 0, 0), 1.0,
            PowerDataRepo.PowerType.FED_IN)
        repo.addOrReplace(LocalDateTime(2024, 1, 1, 0, 1, 0), 1.0,
            PowerDataRepo.PowerType.FED_IN)

        repo.deleteAll()

        repo.powerData.shouldBeEmpty()

    }

}