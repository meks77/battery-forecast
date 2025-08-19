package at.meks.pv.forecast.battery.calculation

/**
 *
 * @param feedInTariffGrid the amount of money which is paid for fed in kwh by the energy purchase agreement partner
 * @param feedInTariffEnergyCommunity the amount of money which is paid for fed in kwh by the energy community
 * @param percentageAmountDeliveryToCommunity     the percentage amount of energy delivered to the community. 0.2 = 20 %
 */
data class FeedInTariffs(val feedInTariffGrid: Double, val feedInTariffEnergyCommunity: Double, val percentageAmountDeliveryToCommunity: Double) {

}
