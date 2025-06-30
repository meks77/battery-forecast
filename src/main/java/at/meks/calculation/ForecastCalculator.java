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

    Result calculateForecast(String price, String capacity, String cycles, Year year, double feedInTariffGrid, double feedInTariffEnergyCommunity, double percentageAmountDeliveryToCommunity) {
        Result calculationResult;
        PowerData powerData = powerDataRepo.powerData(year);
        var originalPowerdataAggregation = new OriginalPowerDataAggregation(powerData, powerDataRepo.years());
        FeedInTariffs feedInTariffs = new FeedInTariffs(feedInTariffGrid,
                                                        feedInTariffEnergyCommunity,
                                                        percentageAmountDeliveryToCommunity / 100.0);
        FeedInTariffs userInputfeedInTariffs = new FeedInTariffs(feedInTariffGrid,
                                                        feedInTariffEnergyCommunity,
                                                        percentageAmountDeliveryToCommunity);
        if (price == null || capacity == null || cycles == null) {
            calculationResult = new Result(new Forecast(0.30, year,
                                                        5.0, 6000, powerData, feedInTariffs) ,
                                           originalPowerdataAggregation,
                                           new UserInput(0.30, 5.0, 6000, LocalDate.now().getYear(),
                                                         userInputfeedInTariffs));
        } else {
            double inputPrice = Double.parseDouble(price);
            double inputCapacity = Double.parseDouble(capacity);
            int inputCycles = Integer.parseInt(cycles);

            Forecast forecast = new Forecast(inputPrice, year, inputCapacity, inputCycles, powerData, feedInTariffs);
            UserInput userInput = new UserInput(inputPrice,
                                                inputCapacity,
                                                inputCycles,
                                                year.getValue(),
                                                userInputfeedInTariffs);
            calculationResult = new Result(forecast, originalPowerdataAggregation, userInput);
        }
        return calculationResult;
    }


    record Result(Forecast forecast, OriginalPowerDataAggregation originalPowerDataAggregation, UserInput userInput) {

    }
}
