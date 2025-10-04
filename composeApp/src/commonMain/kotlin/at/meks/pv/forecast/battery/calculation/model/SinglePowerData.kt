package at.meks.pv.forecast.battery.calculation.model

import kotlinx.datetime.LocalDateTime

/**
 * This record contains the power data, until to the timestamp.
 * E.g. the timestamp is 1.1.2025 00:15 means, that it contains the power data, of the time
 * between 1.1.2025 00:00:00 and 1.1.2025 00:14:59 inclusive
 *
 * @param timestampUntil timestamp until exclusive, of the power data
 * @param feedInKwh the power which was delivered TO the grid
 * @param consumptionKwh the power which was consumed FROM the grid
 */
data class SinglePowerData(val timestampUntil: LocalDateTime, val feedInKwh: Double, val consumptionKwh: Double) {

}
