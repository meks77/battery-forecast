package at.meks.pv.forecast.battery.powerdata

import at.meks.pv.forecast.battery.Year
import at.meks.pv.forecast.battery.minusSeconds

class PowerDataRepo {

    val dataList = mutableSetOf<SinglePowerData>()

    fun add(singlePowerData: SinglePowerData) {
        dataList.add(singlePowerData)
    }

    fun powerData(year: Year): PowerData {
        return PowerData(dataList
            .filter { powerData -> powerData.timestampUntil.minusSeconds(1).year == year.getValue() }
            .sortedBy(SinglePowerData::timestampUntil)
            .toList())
    }

    fun years(): List<Int> {
        return dataList
            .map { powerData -> powerData.timestampUntil.minusSeconds(1).year }
            .distinct()
            .toList()
    }
}
