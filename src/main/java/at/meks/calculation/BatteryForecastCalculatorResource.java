package at.meks.calculation;

import at.meks.PowerData;
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

import java.time.LocalDate;
import java.util.List;

@Path("/calculate")
public class BatteryForecastCalculatorResource {

    @Location("calculate") Template forecastResult;

    @Inject PowerDataRepo powerDataRepo;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance calculate(@RestQuery String price, @RestQuery String capacity, @RestQuery String cycles, @RestQuery int year) {
        List<PowerData> powerData = powerDataRepo.asList(year);
        var originalPowerdataAggregation = new OriginalPowerDataAggregation(powerData, powerDataRepo.years());
        if (price == null || capacity==null || cycles==null) {
            return forecastResult
                    .data("forecast", new Forecast(new Battery(5.0, 6000, powerData), 0.30))
                    .data("userInput", new UserInput(0.30, 5.0, 6000, LocalDate.now().getYear()))
                    .data("originalPowerDataAggregation", originalPowerdataAggregation);
        }
        double inputPrice = Double.parseDouble(price);
        double inputCapacity = Double.parseDouble(capacity);
        int inputCycles = Integer.parseInt(cycles);
        var battery = new Battery(inputCapacity, inputCycles, powerData);

        return forecastResult
                .data("forecast", new Forecast(battery, inputPrice))
                .data("userInput", new UserInput(inputPrice,
                                                 inputCapacity,
                                                 inputCycles,
                                                 year))
                .data("originalPowerDataAggregation", originalPowerdataAggregation);
    }
}
