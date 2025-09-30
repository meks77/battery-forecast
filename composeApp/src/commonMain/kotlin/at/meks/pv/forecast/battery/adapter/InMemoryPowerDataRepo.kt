package at.meks.pv.forecast.battery.adapter

import at.meks.pv.forecast.battery.PowerDataRepo
import at.meks.pv.forecast.battery.Year
import at.meks.pv.forecast.battery.calculation.model.PowerData
import at.meks.pv.forecast.battery.calculation.model.SinglePowerData
import at.meks.pv.forecast.battery.minusSeconds
import kotlinx.datetime.LocalDateTime

class InMemoryPowerDataRepo: PowerDataRepo {

    val powerData = mutableMapOf<LocalDateTime, SinglePowerData>()

    override fun powerData(year: Year): PowerData {
        return PowerData(powerData
            .values
            .filter { powerData -> powerData.timestampUntil.minusSeconds(1).year == year.getValue() }
            .sortedBy(SinglePowerData::timestampUntil)
            .toList())
    }

    override fun years(): List<Int> {
        return powerData
            .keys
            .map { timestamp -> timestamp.minusSeconds(1).year }
            .distinct()
            .toList()
    }

    override fun addOrReplace(timestamp: LocalDateTime, power: Double, type: PowerDataRepo.PowerType) {
        val singlePowerData = powerData.getOrPut(timestamp, { SinglePowerData(timestamp, 0.0, 0.0) })

        if (type == PowerDataRepo.PowerType.FED_IN) {
                powerData[timestamp] = singlePowerData.copy(fedInKwh = power)
        } else {
            powerData[timestamp] = singlePowerData.copy(consumptionKwh = power)
        }
    }

    override fun deleteAll() {
        powerData.clear()
    }

    override fun size(): Int = powerData.size
}