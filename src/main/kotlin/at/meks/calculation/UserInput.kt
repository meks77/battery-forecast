package at.meks.calculation

data class UserInput(val pricePerKwh: Double, val batteryCapacity: Double, val batteryCycles: Int, val year: Int, val feedInTariffs: FeedInTariffs) {

}
