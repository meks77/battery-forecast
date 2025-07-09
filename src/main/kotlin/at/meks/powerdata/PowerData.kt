package at.meks.powerdata

import at.meks.Year
import at.meks.minusSeconds
import java.util.stream.Stream

class PowerData(val singlePowerData: List<SinglePowerData>) {

    fun powerdataForYear(year: Year) : List<SinglePowerData> {
        return singlePowerData
            .filter { data ->  data.timestampUntil.minusSeconds(1).year == year.getValue()}
    }

    fun stream(): Stream<SinglePowerData> {
        return singlePowerData.stream()
    }

}
