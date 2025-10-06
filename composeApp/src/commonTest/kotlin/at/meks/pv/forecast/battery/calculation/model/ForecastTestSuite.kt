package at.meks.pv.forecast.battery.calculation.model

import at.meks.pv.forecast.battery.Year
import at.meks.pv.forecast.battery.calculation.model.FeedInTariffs
import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.maps.shouldContainExactly
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlin.test.Test

class ForecastTest {

    private val testYear = Year(2024)
    private val inputPrice = 0.25
    private val maxBatteryCapacityKwh = 5.0
    private val batteryLifetimeCycles = 6000
    private val feedInTariffs = FeedInTariffs(
        feedInTariffGrid = 0.08,
        feedInTariffEnergyCommunity = 0.12,
        percentageAmountDeliveryToCommunity = 0.3
    )

    private fun createTestPowerData(): PowerData {
        val powerDataList = mutableListOf<SinglePowerData>()
        
        // Add some test data for 2024
        for (month in 1..12) {
            for (day in 1..5) { // Just 5 days per month for testing
                val timestamp = LocalDateTime(2024, month, day, 12, 0)
                powerDataList.add(
                    SinglePowerData(
                        timestampUntil = timestamp,
                        consumptionKwh = 6.0,
                        feedInKwh = 9.0
                    )
                )
            }
        }
        return PowerData(powerDataList)
    }

    private fun createForecast(): Forecast {
        return Forecast(
            inputPrice = inputPrice,
            year = testYear,
            maxBatteryCapacityKwh = maxBatteryCapacityKwh,
            batteryLifetimeCycles = batteryLifetimeCycles,
            powerData = createTestPowerData(),
            feedInTariffs = feedInTariffs
        )
    }

    @Test
    fun testConsumptionFromGrid() {
        val forecast = createForecast()
        val result = forecast.consumptionFromGrid()
        
        result.shouldContainExactly(mapOf(
            Pair(YearMonth(2024, Month.JANUARY), 10.0),
            Pair(YearMonth(2024, Month.FEBRUARY), 5.0),
            Pair(YearMonth(2024, Month.MARCH), 5.0),
            Pair(YearMonth(2024, Month.APRIL), 5.0),
            Pair(YearMonth(2024, Month.MAY), 5.0),
            Pair(YearMonth(2024, Month.JUNE), 5.0),
            Pair(YearMonth(2024, Month.JULY), 5.0),
            Pair(YearMonth(2024, Month.AUGUST), 5.0),
            Pair(YearMonth(2024, Month.SEPTEMBER), 5.0),
            Pair(YearMonth(2024, Month.OCTOBER), 5.0),
            Pair(YearMonth(2024, Month.NOVEMBER), 5.0),
            Pair(YearMonth(2024, Month.DECEMBER), 5.0),
            ))
    }

    @Test
    fun testConsumptionKwh() {
        val forecast = createForecast()
        val result = forecast.consumptionKwh()
        result.shouldBeExactly(65.0)
    }

    @Test
    fun testFeedInPerMonth() {
        val forecast = createForecast()
        val result = forecast.feedInPerMonth()

        result.shouldContainExactly(mapOf(
            Pair(YearMonth(2024, Month.JANUARY), 20.0),
            Pair(YearMonth(2024, Month.FEBRUARY), 20.0),
            Pair(YearMonth(2024, Month.MARCH), 20.0),
            Pair(YearMonth(2024, Month.APRIL), 20.0),
            Pair(YearMonth(2024, Month.MAY), 20.0),
            Pair(YearMonth(2024, Month.JUNE), 20.0),
            Pair(YearMonth(2024, Month.JULY), 20.0),
            Pair(YearMonth(2024, Month.AUGUST), 20.0),
            Pair(YearMonth(2024, Month.SEPTEMBER), 20.0),
            Pair(YearMonth(2024, Month.OCTOBER), 20.0),
            Pair(YearMonth(2024, Month.NOVEMBER), 20.0),
            Pair(YearMonth(2024, Month.DECEMBER), 20.0),
        ))
        
    }

    @Test
    fun testConsumptionFromBatteryKwh() {
        val forecast = createForecast()
        val result = forecast.consumptionFromBatteryKwh()
        // months(12) * daysPerMonth(5) * batteryCapacity(5.0) - batteryCapacity(5.0)
        result.shouldBeExactly(295.0)
    }

    @Test
    fun testFedInKwh() {
        val forecast = createForecast()
        val result = forecast.fedInKwh()
        result.shouldBeExactly(240.0)
    }

    @Test
    fun testBatteryCycles() {
        val forecast = createForecast()
        val result = forecast.batteryCycles()
        // each day the battery is fully discharged and charged again expect on the first day
        result.shouldBeExactly(59.0)
    }

    @Test
    fun testBatteryLifetimeLeftPercent() {
        val forecast = createForecast()
        val result = forecast.batteryLifetimeLeftPercent()
        
        val expectedPercent = 100.0 - 100.0 / batteryLifetimeCycles * forecast.batteryCycles()
        result.shouldBeExactly(expectedPercent)
    }

    @Test
    fun testEstimatedLifetimeYears() {
        val forecast = createForecast()
        val result = forecast.estimatedLifetimeYears()
        result.shouldBeExactly(batteryLifetimeCycles / 59.0)
    }

    @Test
    fun testFedInKwhToEnergyPurchaseAgreementPartner() {
        val forecast = createForecast()
        val result = forecast.fedInKwhToEnergyPurchaseAgreementPartner()
        
        val expectedAmount = forecast.fedInKwh() * (1.0 - feedInTariffs.percentageAmountDeliveryToCommunity)
        result.shouldBeExactly(expectedAmount)
    }

    @Test
    fun testLostFeedInMoney() {
        val forecast = createForecast()
        val result = forecast.lostFeedInMoney()
        
        val expectedLostMoney = forecast.consumptionFromBatteryKwh() * (1.0 - feedInTariffs.percentageAmountDeliveryToCommunity) * feedInTariffs.feedInTariffGrid +
                forecast.consumptionFromBatteryKwh() * feedInTariffs.percentageAmountDeliveryToCommunity * feedInTariffs.feedInTariffEnergyCommunity
        result.shouldBeExactly(expectedLostMoney)
    }

    @Test
    fun testSavedMoneyBecauseOfSavedPower() {
        val forecast = createForecast()
        val result = forecast.savedMoneyBecauseOfSavedPower()
        result.shouldBeExactly(forecast.consumptionFromBatteryKwh() * inputPrice)
    }

    @Test
    fun testSavedMoneyPerYear() {
        val forecast = createForecast()
        val result = forecast.savedMoneyPerYear()
        
        val expectedSavedMoney = forecast.savedMoneyBecauseOfSavedPower() - forecast.lostFeedInMoney()
        result.shouldBeExactly(expectedSavedMoney)
    }

    @Test
    fun testSavedMoneyPerLifetime() {
        val forecast = createForecast()
        val result = forecast.savedMoneyPerLifetime()
        
        // Should be calculated correctly
        val expectedSavedMoney = forecast.savedMoneyPerYear() * forecast.estimatedLifetimeYears()
        result.shouldBeExactly(expectedSavedMoney)
    }

    @Test
    fun testRemainingBatteryPower() {
        val forecast = createForecast()
        val remainingPower = forecast.remainingBatteryPower()
        remainingPower.shouldBeExactly(5.0)
    }

    @Test
    fun testForecastWithEmptyPowerData() {
        val emptyPowerData = PowerData(emptyList())
        val forecast = Forecast(
            inputPrice = inputPrice,
            year = testYear,
            maxBatteryCapacityKwh = maxBatteryCapacityKwh,
            batteryLifetimeCycles = batteryLifetimeCycles,
            powerData = emptyPowerData,
            feedInTariffs = feedInTariffs
        )
        
        forecast.consumptionKwh().shouldBeExactly(0.0)
        forecast.consumptionFromBatteryKwh().shouldBeExactly(0.0)
        forecast.fedInKwh().shouldBeExactly(0.0)
        forecast.batteryCycles().shouldBeExactly(0.0)
    }

    @Test
    fun testConsumptionFromGridWithPartialYearPowerData() {
        val differentYear = Year(2025)
        val powerDataList = mutableListOf<SinglePowerData>()
        
        // Add data for 2025
        powerDataList.add(
            SinglePowerData(
                timestampUntil = LocalDateTime(2025, 1, 1, 12, 0),
                consumptionKwh = 1.0,
                feedInKwh = 1.0
            )
        )
        
        val powerData = PowerData(powerDataList)
        val forecast = Forecast(
            inputPrice = inputPrice,
            year = differentYear,
            maxBatteryCapacityKwh = maxBatteryCapacityKwh,
            batteryLifetimeCycles = batteryLifetimeCycles,
            powerData = powerData,
            feedInTariffs = feedInTariffs
        )
        
        // Should only include data from the specified year
        val consumptionMap = forecast.consumptionFromGrid()
        consumptionMap.shouldContainExactly(mapOf(
            Pair(YearMonth(2025, Month.JANUARY), 1.0),
            Pair(YearMonth(2025, Month.FEBRUARY), 0.0),
            Pair(YearMonth(2025, Month.MARCH), 0.0),
            Pair(YearMonth(2025, Month.APRIL), 0.0),
            Pair(YearMonth(2025, Month.MAY), 0.0),
            Pair(YearMonth(2025, Month.JUNE), 0.0),
            Pair(YearMonth(2025, Month.JULY), 0.0),
            Pair(YearMonth(2025, Month.AUGUST), 0.0),
            Pair(YearMonth(2025, Month.SEPTEMBER), 0.0),
            Pair(YearMonth(2025, Month.OCTOBER), 0.0),
            Pair(YearMonth(2025, Month.NOVEMBER), 0.0),
            Pair(YearMonth(2025, Month.DECEMBER), 0.0)
        ))
    }

    @Test
    fun testFeedInWithPartialYearPowerData() {
        val differentYear = Year(2025)
        val powerDataList = mutableListOf<SinglePowerData>()

        // Add data for 2025
        powerDataList.add(
            SinglePowerData(
                timestampUntil = LocalDateTime(2025, 1, 1, 12, 0),
                consumptionKwh = 1.0,
                feedInKwh = 8.0
            )
        )

        val powerData = PowerData(powerDataList)
        val forecast = Forecast(
            inputPrice = inputPrice,
            year = differentYear,
            maxBatteryCapacityKwh = maxBatteryCapacityKwh,
            batteryLifetimeCycles = batteryLifetimeCycles,
            powerData = powerData,
            feedInTariffs = feedInTariffs
        )

        // Should only include data from the specified year
        val consumptionMap = forecast.feedInPerMonth()
        consumptionMap.shouldContainExactly(mapOf(
            Pair(YearMonth(2025, Month.JANUARY), 3.0),
            Pair(YearMonth(2025, Month.FEBRUARY), 0.0),
            Pair(YearMonth(2025, Month.MARCH), 0.0),
            Pair(YearMonth(2025, Month.APRIL), 0.0),
            Pair(YearMonth(2025, Month.MAY), 0.0),
            Pair(YearMonth(2025, Month.JUNE), 0.0),
            Pair(YearMonth(2025, Month.JULY), 0.0),
            Pair(YearMonth(2025, Month.AUGUST), 0.0),
            Pair(YearMonth(2025, Month.SEPTEMBER), 0.0),
            Pair(YearMonth(2025, Month.OCTOBER), 0.0),
            Pair(YearMonth(2025, Month.NOVEMBER), 0.0),
            Pair(YearMonth(2025, Month.DECEMBER), 0.0)
        ))
    }
}
