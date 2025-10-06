package at.meks.pv.forecast.battery.calculation.model

import at.meks.pv.forecast.battery.Year
import at.meks.pv.forecast.battery.calculation.model.FeedInTariffs
import at.meks.pv.forecast.battery.createLogger
import kotlinx.datetime.YearMonth

class Forecast private constructor(

    private val inputPrice: Double,
    private val year: Year,
    private val photovoltaikSystem: PhotovoltaikSystem,
    private val feedInTariffs: FeedInTariffs) {

    constructor(inputPrice: Double, year:Year, maxBatteryCapacityKwh:Double, batteryLifetimeCycles:Int,
                     powerData:PowerData,  feedInTariffs:FeedInTariffs)
            : this(inputPrice, year, PhotovoltaikSystem(Battery(maxBatteryCapacityKwh, batteryLifetimeCycles)), feedInTariffs) {
        powerData.powerdataForYear(year).forEach(photovoltaikSystem::add)
    }

    fun consumptionFromGrid(): Map<YearMonth, Double> {
        val filteredConsumption = photovoltaikSystem.consumptionFromGrid()
            .filter { entry -> entry.key.year == this.year.getValue() }
        val addMissingMonths = addMissingMonths(filteredConsumption)
        return addMissingMonths.toMap()
    }

    private fun addMissingMonths(
        map: Map<YearMonth, Double>): Map<YearMonth, Double> {
        val result = map.toMutableMap()
        for (i in 1..12) {
            val month = YearMonth(year.getValue(), i)
            if (!result.containsKey(month)) {
                result.put(month, 0.0)
            }
        }
        return result.toMap()
    }

    fun consumptionKwh(): Double {
        return consumptionFromGrid().filter { yearMonth -> yearMonth.key.year == year.getValue() }.values.sum()
    }

    fun feedInPerMonth(): Map<YearMonth, Double>  {
        val filteredEntries = photovoltaikSystem.feedInToGrid()
            .filter { entry -> entry.key.year == this.year.getValue() }
        return addMissingMonths(filteredEntries)
    }

    fun consumptionFromBatteryKwh(): Double {
        return battery().usedKwh()
    }

    fun fedInKwh(): Double {
        return photovoltaikSystem.feedInKwh()
    }

    fun batteryCycles(): Double {
        return battery().batteryCycles()
    }

    fun batteryLifetimeLeftPercent(): Double {
        return 100.0 - 100.0 / battery().lifetimeCycles * battery().batteryCycles()
    }

    private fun battery(): Battery = photovoltaikSystem.battery

    fun estimatedLifetimeYears(): Double {
        return 100 / (100.0 / battery().lifetimeCycles * battery().batteryCycles())
    }

    fun lostFeedInMoney(): Double {
        return consumptionFromBatteryKwh() * (1.0 - feedInTariffs.percentageAmountDeliveryToCommunity) * feedInTariffs.feedInTariffGrid +
                consumptionFromBatteryKwh() * feedInTariffs.percentageAmountDeliveryToCommunity * feedInTariffs.feedInTariffEnergyCommunity
    }

    fun fedInKwhToEnergyPurchaseAgreementPartner(): Double {
        return fedInKwh() * (1.0 - feedInTariffs.percentageAmountDeliveryToCommunity)
    }

    fun fedInKwhToCommunity(): Double {
        return fedInKwh() * feedInTariffs.percentageAmountDeliveryToCommunity
    }

    fun savedMoneyPerYear(): Double {
        return savedMoneyBecauseOfSavedPower() - lostFeedInMoney()
    }

    fun savedMoneyBecauseOfSavedPower(): Double {
        return inputPrice * battery().usedKwh()
    }

    fun savedMoneyPerLifetime(): Double {
        return savedMoneyPerYear() * estimatedLifetimeYears()
    }

    fun remainingBatteryPower(): Double {
        return battery().currentBatteryPower()
    }

}
