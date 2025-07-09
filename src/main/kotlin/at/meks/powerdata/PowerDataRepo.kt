package at.meks.powerdata

import jakarta.enterprise.context.ApplicationScoped
import at.meks.Year
import at.meks.minusSeconds

@ApplicationScoped
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
