package at.meks.calculation;

import at.meks.powerdata.PowerData;
import at.meks.powerdata.PowerDataRepo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.time.Year;

@ApplicationScoped
class ForecastCalculator {

    @Inject PowerDataRepo powerDataRepo;

    Result calculateForecast(String price, String capacity, String cycles, Year year) {
        Result calculationResult;
        PowerData powerData = powerDataRepo.powerData(year);
        var originalPowerdataAggregation = new OriginalPowerDataAggregation(powerData, powerDataRepo.years());
        if (price == null || capacity == null || cycles == null) {
            calculationResult = new Result(new Forecast(0.30, year,
                                                        5.0, 6000, powerData) ,
                                           originalPowerdataAggregation,
                                           new UserInput(0.30, 5.0, 6000, LocalDate.now().getYear()));
        } else {
            double inputPrice = Double.parseDouble(price);
            double inputCapacity = Double.parseDouble(capacity);
            int inputCycles = Integer.parseInt(cycles);

            Forecast forecast = new Forecast(inputPrice, year, inputCapacity, inputCycles, powerData);
            UserInput userInput = new UserInput(inputPrice,
                                                inputCapacity,
                                                inputCycles,
                                                year.getValue());
            calculationResult = new Result(forecast, originalPowerdataAggregation, userInput);
        }
        return calculationResult;
    }

    record Result(Forecast forecast, OriginalPowerDataAggregation originalPowerDataAggregation, UserInput userInput) {

    }
}
