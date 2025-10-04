package at.meks.pv.forecast.battery.calculation.model

import at.meks.pv.forecast.battery.Year
import at.meks.pv.forecast.battery.calculation.FeedInTariffs
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
        return photovoltaikSystem.consumptionFromGrid()
            .filter { entry -> entry.key.year == this.year.getValue() }
    }

    private fun consumptionFromGridPerMonth(): List<Double> {
        val resultMap = HashMap<YearMonth, Double>()
        for (i in 1..12) {
            resultMap.put(YearMonth(year.getValue(), i), 0.0)
        }
        photovoltaikSystem.consumptionFromGrid()
                .filter{entry -> entry.key.year == this.year.getValue()}
                .forEach{entry -> resultMap.put(entry.key, entry.value)}
        return resultMap.entries
            .sortedBy { entry -> entry.key }
            .map { entry -> entry.value }
    }

    fun consumptionKwh(): Double {
        return consumptionFromGridPerMonth().sum()
    }

    fun feedInPerMonth(): Map<YearMonth, Double>  {
        return photovoltaikSystem.feedInToGrid()
            .filter { entry -> entry.key.year == this.year.getValue() }
    }

    fun usedKwh(): Double {
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
        return fedInKwhToEnergyPurchaseAgreementPartner() * feedInTariffs.feedInTariffGrid +
                fedInKwhToCommunity() * feedInTariffs.feedInTariffEnergyCommunity
    }

    fun fedInKwhToEnergyPurchaseAgreementPartner(): Double {
        return battery().usedKwh() * (1.0 - feedInTariffs.percentageAmountDeliveryToCommunity)
    }

    private fun fedInKwhToCommunity(): Double {
        return battery().usedKwh() * feedInTariffs.percentageAmountDeliveryToCommunity
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
