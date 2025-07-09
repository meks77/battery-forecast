package at.meks.calculation

import at.meks.powerdata.SinglePowerData
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.YearMonth

class Inverter(val battery: Battery) {

    val logger = LoggerFactory.getLogger(Inverter::class.java)

    var fedInKwh: Double = 0.0

    var consumptionFromGrid = mutableMapOf<YearMonth, Double>()
    var fedInToGrid = mutableMapOf<YearMonth, Double>()

    fun consumptionFromGrid(): Map<YearMonth, Double> {
        return consumptionFromGrid
    }

    fun fedInToGrid(): Map<YearMonth, Double> {
        return fedInToGrid
    }

    fun fedInKwh(): Double {
        return fedInKwh
    }

    fun add(singlePowerData: SinglePowerData) {
        consume(singlePowerData.timestampUntil, singlePowerData.consumptionKwh)
        save(singlePowerData.timestampUntil, singlePowerData.fedInKwh)
    }

    fun consume(timestamp: LocalDateTime, kwh: Double) {
        val consumedFromBattery = battery.consume(kwh)
        val consumedFromGrid = kwh - consumedFromBattery
        consumptionFromGrid.compute(YearMonth.of(timestamp.year, timestamp.month)
        ) { month, currentConsumption ->
            if (currentConsumption == null) consumedFromGrid else currentConsumption + consumedFromGrid
        }
        logger.debug("consumed kwh {}; currentCapacity: {}; usedKwh: {}", kwh, battery.currentBatteryPower(),
            battery.usedKwh())
    }

    private fun save(timestamp: LocalDateTime, kwh: Double) {
        val savedKwh = battery.save(kwh)
        var fedInKwh = 0.0
        if (savedKwh < kwh) {
            fedInKwh = kwh - savedKwh
        }
        this.fedInKwh += fedInKwh
        val furtherFedInForMonth = fedInKwh
        fedInToGrid.compute(YearMonth.of(timestamp.year, timestamp.month)
        ) { month, currentValue -> if (currentValue == null) furtherFedInForMonth else currentValue + furtherFedInForMonth }
        logger.debug("saved kwh {}; currentCapacity: {}; usedKwh: {}", kwh, battery.currentBatteryPower(),
            battery.usedKwh())
    }
}
