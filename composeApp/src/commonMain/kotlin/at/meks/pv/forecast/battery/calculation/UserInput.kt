package at.meks.pv.forecast.battery.calculation

data class UserInput(val pricePerKwh: Double, val batteryCapacity: Double, val batteryCycles: Int, val year: Int, val feedInTariffs: FeedInTariffs) {

}
