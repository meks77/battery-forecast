package at.meks.pv.forecast.battery.specs

import at.meks.pv.forecast.battery.Year
import at.meks.pv.forecast.battery.calculation.model.FeedInTariffs
import at.meks.pv.forecast.battery.calculation.model.Forecast
import at.meks.pv.forecast.battery.calculation.model.PowerData
import at.meks.pv.forecast.battery.calculation.model.SinglePowerData
import at.meks.pv.forecast.battery.calculation.round
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.char

abstract class CalculationFixtureBase {

    private val powerDataList = mutableListOf<SinglePowerData>()
    private var forecast: Forecast? = null

    fun addEnergyData(timestamp: String, consumption: String, feedIn: String) {
        val customFormat = LocalDateTime.Format {
            day()
            char('.')
            monthNumber()
            char('.')
            year()
            char(' ')
            hour(); char(':'); minute()
        }
        powerDataList += SinglePowerData(
            timestampUntil = LocalDateTime.parse(timestamp.trim(), customFormat),
            feedInKwh = feedIn.toDouble(),
            consumptionKwh = consumption.toDouble()
        )
    }

    fun calculateForecast(year: String, kapazitaetKwh: Double, preisProKwh: Double,
                          einspeisetarifNetz: Double, einspeisetarifGemeinschaft: Double,
                          prozentZurGemeinschaft: Double) {
        val pd = PowerData(powerDataList.toList())
        forecast = Forecast(preisProKwh, Year(year.toInt()), kapazitaetKwh, 6000, pd,
            FeedInTariffs(einspeisetarifNetz, einspeisetarifGemeinschaft, prozentZurGemeinschaft))
    }

    fun consumptionFromGrid(): Double = forecast().consumptionKwh()
    fun feedInToContractPartner(): Double = forecast().fedInKwh()
    fun consumptionFromBattery(): Double = forecast().consumptionFromBatteryKwh()
    fun resumingEnergyInBattery(): Double = forecast().remainingBatteryPower()
    fun savedMoney(): Double = forecast().savedMoneyPerYear().round(2)
    fun lostMoneyNotFedIn(): Double = forecast().lostFeedInMoney()

    private fun forecast(): Forecast = forecast!!

}