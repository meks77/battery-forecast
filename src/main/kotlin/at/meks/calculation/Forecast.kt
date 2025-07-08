package at.meks.calculation

import at.meks.powerdata.PowerData
import java.time.Year
import java.time.YearMonth

class Forecast private constructor(

    private val inputPrice: Double,
    private val year: Year,
    private val inverter: Inverter,
    private val feedInTariffs: FeedInTariffs) {

    constructor(inputPrice: Double, year:Year, maxBatteryCapacityKwh:Double, batteryLifetimeCycles:Int,
                     powerData:PowerData,  feedInTariffs:FeedInTariffs)
            : this(inputPrice, year, Inverter(Battery(maxBatteryCapacityKwh, batteryLifetimeCycles)), feedInTariffs) {
        powerData.powerdataForYear(year).forEach(inverter::add)
    }

    fun consumptionFromGridPerMonthAsString(): String {
        return consumptionFromGridPerMonth()
            .joinToString(" ,") { d -> d.toString() }
    }

    private fun consumptionFromGridPerMonth(): List<Double> {
        val resultMap = HashMap<YearMonth, Double>()
        for (i in 1..12) {
            resultMap.put(YearMonth.of(year.value, i), 0.0)
        }
        inverter.consumptionFromGrid()
                .filter{entry -> entry.key.year == this.year.value}
                .forEach{entry -> resultMap.put(entry.key, entry.value)}
        return resultMap.toSortedMap()
            .map { entry -> entry.value }
    }

    fun consumptionKwh(): Double {
        return consumptionFromGridPerMonth().sum()
    }

    fun fedInPerMonthAsString(): String {
        return inverter.fedInToGrid()
            .toSortedMap()
            .map { entry -> entry.value }
            .map { value -> value * -1.0 }
            .joinToString(", ") { value -> value.toString() }
    }

    fun usedKwh(): Double {
        return battery().usedKwh()
    }

    fun fedInKwh(): Double {
        return inverter.fedInKwh()
    }

    fun batteryCycles(): Double {
        return battery().batteryCycles()
    }

    fun batteryLifetimeLeftPercent(): Double {
        return 100.0 - 100.0 / battery().lifetimeCycles * battery().batteryCycles()
    }

    private fun battery(): Battery = inverter.battery

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
