package at.meks.pv.forecast.battery.calculation

import at.meks.pv.forecast.battery.calculation.model.PowerData
import at.meks.pv.forecast.battery.calculation.model.SinglePowerData
import at.meks.pv.forecast.battery.createLogger
import at.meks.pv.forecast.battery.minusSeconds
import kotlinx.datetime.Month

class OriginalPowerDataAggregation(val years: List<Int>) {

    private val consumptionFromGrid = mutableMapOf<Month, Double>()
    private val fedInToGrid = mutableMapOf<Month, Double>()
    private val logger = createLogger(this)

    constructor(powerData: PowerData, years: List<Int>) : this(years) {
        Month.entries.forEach { month -> this.consumptionFromGrid[month] = 0.0 }
        Month.entries.forEach { month -> this.fedInToGrid[month] = 0.0 }
        powerData.getPowerData().forEach(this::calculatePowerStatistics)
    }

    private fun calculatePowerStatistics(singlePowerData: SinglePowerData) {
        val month = singlePowerData.timestampUntil.minusSeconds(1).month
        consumptionFromGrid[month] = (consumptionFromGrid[month]?:0.0) + singlePowerData.consumptionKwh
        fedInToGrid[month] = (fedInToGrid[month]?:0.0) + singlePowerData.feedInKwh
        logger.debug("modified power data by $singlePowerData to $consumptionFromGrid and $fedInToGrid")
    }

    fun consumptionPerMonth(): Map<Month, Double> {
        logger.debug("consumptionFromGrid: $consumptionFromGrid")
        return consumptionFromGrid.toMap()
    }

    fun fedInPerMonth(): Map<Month, Double> {
        logger.debug("fedInPerMonth: $fedInToGrid")
        val result = mutableMapOf<Month, Double>()
        fedInToGrid
            .forEach { entry -> result.put(entry.key, entry.value * -1.0) }
        return result.toMap()
    }

    fun consumptionOfYear(): Double {
        return consumptionFromGrid.values.sum()
    }

    fun fedInOfYear(): Double {
        return fedInToGrid.values.sum()
    }

}
