package at.meks.pv.forecast.battery.calculation

import at.meks.pv.forecast.battery.calculation.model.PowerData
import at.meks.pv.forecast.battery.calculation.model.SinglePowerData
import kotlinx.datetime.Month

class OriginalPowerDataAggregation(val years: List<Int>) {

    private val consumptionFromGrid = mutableMapOf<Month, Double>()
    private val fedInToGrid = mutableMapOf<Month, Double>()

    constructor(powerData: PowerData, years: List<Int>) : this(years) {
        Month.entries.forEach { month -> this.consumptionFromGrid[month] = 0.0 }
        Month.entries.forEach { month -> this.fedInToGrid[month] = 0.0 }
        powerData.getPowerData().forEach(this::calculatePowerStatistics)
    }

    private fun calculatePowerStatistics(singlePowerData: SinglePowerData) {
        consumptionFromGrid[singlePowerData.timestampUntil.month] = singlePowerData.consumptionKwh
        fedInToGrid[singlePowerData.timestampUntil.month] = singlePowerData.feedInKwh
    }

    fun consumptionPerMonthAsString(): String {
        return consumptionFromGrid.entries
            .joinToString(separator = ", ") { it.value.toString() }
    }

    fun fedInPerMonthAsString(): String {
        return fedInToGrid.entries
            .map { it.value * -1.0 }
            .joinToString(separator = ", ") { it.toString() }
    }

    fun consumptionOfYear(): Double {
        return consumptionFromGrid.values.sum()
    }

    fun fedInOfYear(): Double {
        return fedInToGrid.values.sum()
    }

}
