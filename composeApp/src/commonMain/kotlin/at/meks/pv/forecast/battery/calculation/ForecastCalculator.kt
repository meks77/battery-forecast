package at.meks.pv.forecast.battery.calculation

import at.meks.pv.forecast.battery.PowerDataRepo
import at.meks.pv.forecast.battery.Year
import at.meks.pv.forecast.battery.calculation.model.FeedInTariffs
import at.meks.pv.forecast.battery.calculation.model.Forecast
import at.meks.pv.forecast.battery.createLogger

class ForecastCalculator(val powerDataRepo: PowerDataRepo) {


    fun calculateForecast(price: Double, capacity: Double, cycles: Int, year: Year, feedInTariffGrid: Double,
                          feedInTariffEnergyCommunity: Double, percentageAmountDeliveryToCommunity: Double
    ): Result {
        val yearForCalculation = yearForCalculation(year)
        val powerData = powerDataRepo.powerData(yearForCalculation)
        val feedInTariffs =
            FeedInTariffs(feedInTariffGrid, feedInTariffEnergyCommunity, percentageAmountDeliveryToCommunity)

        val forecast = Forecast(price, yearForCalculation, capacity, cycles, powerData, feedInTariffs)

        val originalPowerdataAggregation = OriginalPowerDataAggregation(powerData, powerDataRepo.years())
        val userInputfeedInTariffs =
            feedInTariffs.copy(percentageAmountDeliveryToCommunity = percentageAmountDeliveryToCommunity)
        createLogger("ForecastCalculator").debug("DelToCommunity: $percentageAmountDeliveryToCommunity, ${feedInTariffs.percentageAmountDeliveryToCommunity}, ${userInputfeedInTariffs.percentageAmountDeliveryToCommunity}")
        val userInput = UserInput(price, capacity, cycles, yearForCalculation.getValue(), userInputfeedInTariffs)
        return Result(forecast, originalPowerdataAggregation, userInput)
    }

    private fun yearForCalculation(year: Year?): Year {
        return year
            ?: if (powerDataRepo.years().isNotEmpty()){
                Year(powerDataRepo.years().last())
            } else {
                Year.now()
            }
    }

    data class Result(
        val forecast: Forecast,
        val originalPowerDataAggregation: OriginalPowerDataAggregation,
        val userInput: UserInput
    )
}
