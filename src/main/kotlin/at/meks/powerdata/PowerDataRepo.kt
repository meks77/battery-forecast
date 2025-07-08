package at.meks.powerdata

import jakarta.enterprise.context.ApplicationScoped
import java.time.Year

@ApplicationScoped
class PowerDataRepo {

    val dataList = mutableSetOf<SinglePowerData>()

    fun add(singlePowerData: SinglePowerData) {
        dataList.add(singlePowerData)
    }

    fun powerData(year: Year): PowerData {
        return PowerData(dataList
            .filter { powerData -> powerData.timestampUntil.minusSeconds(1).getYear() == year.getValue() }
            .sortedBy(SinglePowerData::timestampUntil)
            .toList())
    }

    fun years(): List<Int> {
        return dataList
            .map { powerData -> powerData.timestampUntil.minusSeconds(1).getYear() }
            .distinct()
            .toList()
    }
}
