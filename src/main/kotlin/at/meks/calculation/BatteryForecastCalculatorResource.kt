package at.meks.calculation

import io.quarkus.qute.Location
import io.quarkus.qute.Template
import io.quarkus.qute.TemplateInstance
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.jboss.resteasy.reactive.RestQuery
import java.time.Year

@Path("/calculate")
class BatteryForecastCalculatorResource(@Location("calculate") private val forecastResult: Template,
                                        private val forecastCalculator: ForecastCalculator) {

    @GET
    @Produces(MediaType.TEXT_HTML)
    fun calculate(
        @RestQuery price: String?, @RestQuery capacity: String?, @RestQuery cycles: String?,
        @RestQuery year: Integer?, @RestQuery feedInTariffGrid: String?,
        @RestQuery feedInTariffEnergyCommunity: String?,
        @RestQuery percentageAmountDeliveryToCommunity: String?
    ): TemplateInstance {
        val calculationResult = forecastCalculator.calculateForecast(
            price?.toDouble() ?: 0.28,
            capacity?.toDouble() ?: 5.0,
            cycles?.toInt() ?: 6000,
            asYear(year),
            feedInTariffGrid?.toDouble() ?: 0.055,
            feedInTariffEnergyCommunity?.toDouble() ?: 0.1,
            percentageAmountDeliveryToCommunity?.toDouble() ?: 20.0
        )
        return forecastResult
            .data("forecast", calculationResult.forecast)
            .data("userInput", calculationResult.userInput)
            .data("originalPowerDataAggregation", calculationResult.originalPowerDataAggregation)
    }

    private fun asYear(year: Integer?): Year {
        if (year == null) {
            return Year.now()
        }
        return Year.of(year.toInt())
    }
}
