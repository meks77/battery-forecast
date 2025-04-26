package at.meks.calculation;

import at.meks.PowerData;
import at.meks.PowerDataRepo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
class ForecastCalculator {

    @Inject PowerDataRepo powerDataRepo;

    Result calculateForecast(String price, String capacity, String cycles, int year) {
        Result calculationResult;
        List<PowerData> powerData = powerDataRepo.asList(year);
        var originalPowerdataAggregation = new OriginalPowerDataAggregation(powerData, powerDataRepo.years());
        if (price == null || capacity == null || cycles == null) {
            calculationResult = new Result(new Forecast(new Battery(5.0, 6000, powerData), 0.30),
                                           originalPowerdataAggregation,
                                           new UserInput(0.30, 5.0, 6000, LocalDate.now().getYear()));
        } else {
            double inputPrice = Double.parseDouble(price);
            double inputCapacity = Double.parseDouble(capacity);
            int inputCycles = Integer.parseInt(cycles);
            var battery = new Battery(inputCapacity, inputCycles, powerData);

            Forecast forecast = new Forecast(battery, inputPrice);
            UserInput userInput = new UserInput(inputPrice,
                                                inputCapacity,
                                                inputCycles,
                                                year);
            calculationResult = new Result(forecast, originalPowerdataAggregation, userInput);
        }
        return calculationResult;
    }

    record Result(Forecast forecast, OriginalPowerDataAggregation originalPowerDataAggregation, UserInput userInput) {

    }
}
