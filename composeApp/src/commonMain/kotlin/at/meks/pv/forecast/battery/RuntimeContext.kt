package at.meks.pv.forecast.battery

import at.meks.pv.forecast.battery.adapter.InMemoryPowerDataRepo

class RuntimeContext private constructor() {

    companion object {
        private val currentInstance = RuntimeContext()
        fun currentContext() = currentInstance
    }

    private val powerDataRepo = InMemoryPowerDataRepo()

    fun powerDataRepo(): PowerDataRepo = powerDataRepo

}
