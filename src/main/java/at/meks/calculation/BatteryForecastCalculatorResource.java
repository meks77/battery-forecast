package at.meks.calculation;

import at.meks.powerdataupload.PowerDataRepo;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestQuery;

@Path("/calculate")
public class BatteryForecastCalculatorResource {

    @Location("calculate") Template forecastResult;

    @Inject ForecastCalculator forecastCalculator;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance calculate(@RestQuery String price, @RestQuery String capacity, @RestQuery String cycles, @RestQuery int year) {
        var calculationResult = forecastCalculator.calculateForecast(price, capacity, cycles, year);
        return forecastResult
                .data("forecast", calculationResult.forecast())
                .data("userInput", calculationResult.userInput())
                .data("originalPowerDataAggregation", calculationResult.originalPowerDataAggregation());
    }
}
