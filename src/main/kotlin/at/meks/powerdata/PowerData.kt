package at.meks.powerdata

import at.meks.Year
import at.meks.minusSeconds
import io.quarkus.qute.ImmutableList

class PowerData(private val singlePowerData: List<SinglePowerData>) {

    fun powerdataForYear(year: Year) : List<SinglePowerData> {
        return singlePowerData
            .filter { data ->  data.timestampUntil.minusSeconds(1).year == year.getValue()}
    }

    fun getPowerData(): List<SinglePowerData> {
        return ImmutableList.copyOf(singlePowerData)
    }

}
