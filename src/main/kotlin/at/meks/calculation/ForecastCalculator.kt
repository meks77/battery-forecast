package at.meks.calculation

import at.meks.powerdata.PowerDataRepo
import jakarta.enterprise.context.ApplicationScoped
import java.time.Year

@ApplicationScoped
class ForecastCalculator(val powerDataRepo: PowerDataRepo) {

    fun calculateForecast(price: Double, capacity: Double, cycles: Int, year: Year, feedInTariffGrid: Double,
                          feedInTariffEnergyCommunity: Double, percentageAmountDeliveryToCommunity: Double
    ): Result {
        val powerData = powerDataRepo.powerData(year)
        val feedInTariffs =
            FeedInTariffs(feedInTariffGrid, feedInTariffEnergyCommunity, percentageAmountDeliveryToCommunity / 100.0)

        val forecast = Forecast(price, year, capacity, cycles, powerData, feedInTariffs)

        val originalPowerdataAggregation = OriginalPowerDataAggregation(powerData, powerDataRepo.years())
        val userInputfeedInTariffs =
            feedInTariffs.copy(percentageAmountDeliveryToCommunity = percentageAmountDeliveryToCommunity)
        val userInput = UserInput(price, capacity, cycles, year.value, userInputfeedInTariffs)
        return Result(forecast, originalPowerdataAggregation, userInput)
    }

    data class Result(
        val forecast: Forecast,
        val originalPowerDataAggregation: OriginalPowerDataAggregation,
        val userInput: UserInput
    )
}
