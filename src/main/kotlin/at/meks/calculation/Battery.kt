package at.meks.calculation

import kotlin.math.min

class Battery(val batteryCapacityKwh: Double, val lifetimeCycles: Int) {

    private var currentBatteryPower: Double = 0.0
    private var usedKwh: Double = 0.0

    fun consume(kwh: Double): Double {
        val removedPower = min(kwh, currentBatteryPower)
        currentBatteryPower -= removedPower
        usedKwh += removedPower
        return removedPower
    }

    fun save(kwh: Double): Double {
        val maxSavableKwh = batteryCapacityKwh - currentBatteryPower
        val savedKwh = min(maxSavableKwh, kwh)
        currentBatteryPower += savedKwh
        return savedKwh
    }

    fun usedKwh(): Double = usedKwh

    fun batteryCycles(): Double = usedKwh / batteryCapacityKwh

    fun currentBatteryPower(): Double = currentBatteryPower

}
