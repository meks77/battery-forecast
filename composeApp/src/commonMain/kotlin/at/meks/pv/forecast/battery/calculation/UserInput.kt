package at.meks.pv.forecast.battery.calculation

import at.meks.pv.forecast.battery.calculation.model.FeedInTariffs

data class UserInput(var pricePerKwh: Double, var batteryCapacity: Double, var batteryCycles: Int, var year: Int, var feedInTariffs: FeedInTariffs) {

}
