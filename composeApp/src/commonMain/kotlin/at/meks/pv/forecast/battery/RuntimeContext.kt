package at.meks.pv.forecast.battery

import at.meks.pv.forecast.battery.adapter.InMemoryPowerDataRepo
import at.meks.pv.forecast.battery.calculation.ForecastCalculator

class RuntimeContext private constructor() {

    companion object {
        private val currentInstance = RuntimeContext()
        fun currentContext() = currentInstance
    }

    private val powerDataRepo = InMemoryPowerDataRepo()

    private val forecastCalculator = ForecastCalculator(powerDataRepo)

    fun powerDataRepo(): PowerDataRepo = powerDataRepo

    fun forecastCalculator(): ForecastCalculator = forecastCalculator

}
