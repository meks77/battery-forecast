package at.meks.pv.forecast.battery.powerdata

import at.meks.pv.forecast.battery.Year
import at.meks.pv.forecast.battery.minusSeconds

class PowerData(private val singlePowerData: List<SinglePowerData>) {

    fun powerdataForYear(year: Year) : List<SinglePowerData> {
        return singlePowerData
            .filter { data ->  data.timestampUntil.minusSeconds(1).year == year.getValue()}
    }

    fun getPowerData(): List<SinglePowerData> {
        return singlePowerData.toList()
    }

}
