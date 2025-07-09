package at.meks.calculation

import at.meks.powerdata.PowerData
import at.meks.powerdata.SinglePowerData
import kotlinx.datetime.Month

class OriginalPowerDataAggregation(val years: List<Int>) {

    private val consumptionFromGrid = mutableMapOf<Month, Double>()
    private val fedInToGrid = mutableMapOf<Month, Double>()

    constructor(powerData: PowerData, years: List<Int>) : this(years) {
        Month.entries.forEach { month -> consumptionFromGrid.put(month, 0.0) }
        Month.entries.forEach { month -> fedInToGrid.put(month, 0.0) }
        powerData.getPowerData().forEach(this::calculatePowerStatistics)
    }

    private fun calculatePowerStatistics(singlePowerData: SinglePowerData) {
        consumptionFromGrid.compute(singlePowerData.timestampUntil.month)
        { month, currentConsumption ->
            if (currentConsumption == null) singlePowerData.consumptionKwh else currentConsumption + singlePowerData.consumptionKwh
        }
        fedInToGrid.compute(singlePowerData.timestampUntil.month)
        { month, currentValue ->
            if (currentValue == null) singlePowerData.fedInKwh else currentValue + singlePowerData.fedInKwh
        }
    }

    fun consumptionPerMonthAsString(): String {
        return consumptionFromGrid.toSortedMap()
            .map { it.value.toString() }
            .joinToString(separator = ", ")
    }

    fun fedInPerMonthAsString(): String {
        return fedInToGrid.toSortedMap()
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
