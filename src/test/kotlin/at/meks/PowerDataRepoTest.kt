package at.meks.calculation

import at.meks.powerdata.PowerDataRepo
import at.meks.powerdata.SinglePowerData
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import at.meks.Year
import kotlinx.datetime.LocalDateTime

class PowerDataRepoTest {

    val repo = PowerDataRepo()

    @Nested
    inner class Years {

        @Test
        fun onlyYearsContainingPowerDataAreReturned() {
            repo.add(SinglePowerData(LocalDateTime(2022, 12, 31, 23, 59), 1.0, 0.0))
            repo.add(SinglePowerData(LocalDateTime(2023, 1, 1, 0, 0), 1.0, 0.0))
            repo.add(SinglePowerData(LocalDateTime(2023, 1, 1, 0, 1), 1.0, 0.0))
            repo.add(SinglePowerData(LocalDateTime(2023, 1, 1, 0, 2), 1.0, 0.0))
            repo.add(SinglePowerData(LocalDateTime(2024, 1, 1, 0, 0), 1.0, 0.0))

            val result = repo.years()
            assertThat(result).containsExactly(2022, 2023)
        }
    }

    @Nested
    inner class AsListOfYear {

        @Test
        fun onlyDataContainingPowerDataWithinTheYearAreReturned() {
            repo.add(SinglePowerData(LocalDateTime(2022, 12, 31, 23, 59), 1.0, 0.0))
            repo.add(SinglePowerData(LocalDateTime(2023, 1, 1, 0, 0), 1.0, 0.0))
            repo.add(SinglePowerData(LocalDateTime(2023, 1, 1, 0, 1), 1.0, 0.0))
            repo.add(SinglePowerData(LocalDateTime(2023, 12, 31, 23, 59, 59), 1.0, 0.0))
            repo.add(SinglePowerData(LocalDateTime(2024, 1, 1, 0, 0, 0), 1.0, 0.0))
            repo.add(SinglePowerData(LocalDateTime(2024, 1, 1, 0, 1, 0), 1.0, 0.0))

            val result = repo.powerData(Year(2023)).stream()

            assertThat(result).containsExactly(
                    SinglePowerData(LocalDateTime(2023, 1, 1, 0, 1), 1.0, 0.0),
                    SinglePowerData(LocalDateTime(2023, 12, 31, 23, 59, 59), 1.0, 0.0),
                    SinglePowerData(LocalDateTime(2024, 1, 1, 0, 0), 1.0, 0.0)
            )
        }
    }


}