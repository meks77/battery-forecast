package at.meks.pv.forecast.battery.calculation.model

import at.meks.pv.forecast.battery.Logger
import at.meks.pv.forecast.battery.createLogger
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.YearMonth

class PhotovoltaikSystem(val battery: Battery) {

    val logger: Logger = createLogger(this)

    var feedInKwh: Double = 0.0

    var consumptionFromGrid = mutableMapOf<YearMonth, Double>()
    var feedInToGrid = mutableMapOf<YearMonth, Double>()

    fun consumptionFromGrid(): Map<YearMonth, Double> {
        return consumptionFromGrid
    }

    fun feedInToGrid(): Map<YearMonth, Double> {
        return feedInToGrid
    }

    fun feedInKwh(): Double {
        return feedInKwh
    }

    fun add(singlePowerData: SinglePowerData) {
        consume(singlePowerData.timestampUntil, singlePowerData.consumptionKwh)
        save(singlePowerData.timestampUntil, singlePowerData.feedInKwh)
    }

    fun consume(timestamp: LocalDateTime, kwh: Double) {
        val consumedFromBattery = battery.consume(kwh)
        val consumedFromGrid = kwh - consumedFromBattery
        val yearMonth = YearMonth(timestamp.year, timestamp.month)
        consumptionFromGrid[yearMonth] = consumptionFromGrid.getOrElse(yearMonth) { 0.0 } + consumedFromGrid
        logger.debug("consumed kwh $kwh; currentCapacity: ${battery.currentBatteryPower()}; usedKwh: ${battery.usedKwh()}")
    }

    private fun save(timestamp: LocalDateTime, kwh: Double) {
        val savedKwh = battery.save(kwh)
        var feedInKwh = 0.0
        if (savedKwh < kwh) {
            feedInKwh = kwh - savedKwh
        }
        this.feedInKwh += feedInKwh
        val furtherFedInForMonth = feedInKwh
        val yearMonth = YearMonth(timestamp.year, timestamp.month)
        feedInToGrid[yearMonth] = feedInToGrid.getOrElse(yearMonth) { 0.0} + furtherFedInForMonth
        logger.debug("saved kwh $kwh; currentCapacity: $battery.currentBatteryPower(); usedKwh: $battery.usedKwh()")
    }
}
