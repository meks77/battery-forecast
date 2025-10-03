package at.meks.pv.forecast.battery.calculation

data class UserInput(var pricePerKwh: Double, var batteryCapacity: Double, var batteryCycles: Int, var year: Int, var feedInTariffs: FeedInTariffs) {

}
