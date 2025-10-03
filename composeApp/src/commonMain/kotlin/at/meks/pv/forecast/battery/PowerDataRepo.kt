package at.meks.pv.forecast.battery

import at.meks.pv.forecast.battery.calculation.model.PowerData
import kotlinx.datetime.LocalDateTime

interface PowerDataRepo {

    enum class PowerType(val description: String) {
        FED_IN("Power Fed In File"), CONSUMPTION("Power Consumption File")
    }

    fun powerData(year: Year): PowerData

    fun years(): List<Int>

    fun addOrReplace(timestamp: LocalDateTime, power: Double, type: PowerType)

    fun deleteAll()

    fun size(): Int


}