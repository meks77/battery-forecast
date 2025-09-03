package at.meks.pv.forecast.battery.calculation

import at.meks.pv.forecast.battery.Year
import at.meks.pv.forecast.battery.calculation.model.PowerData
import at.meks.pv.forecast.battery.calculation.model.SinglePowerData
import at.meks.pv.forecast.battery.minusSeconds
import kotlinx.datetime.LocalDateTime

class PowerDataRepo {
    companion object {
        val POWER_DATA_REPO = PowerDataRepo()
    }

    enum class PowerType(val description: String) {
        FED_IN("Power Fed In File"), CONSUMPTION("Power Consumption File")
    }

    val powerData = mutableMapOf<LocalDateTime, SinglePowerData>()

    fun powerData(year: Year): PowerData {
        return PowerData(powerData
            .values
            .filter { powerData -> powerData.timestampUntil.minusSeconds(1).year == year.getValue() }
            .sortedBy(SinglePowerData::timestampUntil)
            .toList())
    }

    fun years(): List<Int> {
        return powerData
            .keys
            .map { timestamp -> timestamp.minusSeconds(1).year }
            .distinct()
            .toList()
    }

    fun addOrReplace(timestamp: LocalDateTime, power: Double, type: PowerType) {
        val singlePowerData = powerData.getOrPut(timestamp, { SinglePowerData(timestamp, 0.0, 0.0) })

        if (type == PowerType.FED_IN) {
                powerData[timestamp] = singlePowerData.copy(fedInKwh = power)
        } else {
            powerData[timestamp] = singlePowerData.copy(consumptionKwh = power)
        }
    }

    fun deleteAll() {
        powerData.clear()
    }

    fun size(): Int = powerData.size
}