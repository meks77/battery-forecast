package at.meks.calculation;

import at.meks.powerdata.PowerData;

import java.time.Year;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public final class Forecast {

    private final Battery battery;
    private final double inputPrice;
    private final Year year;
    private final Inverter inverter;
    private final FeedInTariffs feedInTariffs;

    public Forecast(double inputPrice, Year year, double maxBatteryCapacityKwh, int batteryLifetimeCycles,
                    PowerData powerData, FeedInTariffs feedInTariffs) {
        this.feedInTariffs = feedInTariffs;
        this.battery = new Battery(maxBatteryCapacityKwh, batteryLifetimeCycles);
        inverter = new Inverter(battery);

        powerData.stream(year).forEach(inverter::add);
        this.inputPrice = inputPrice;
        this.year = year;
    }

    public String consumptionFromGridPerMonthAsString() {
        return DoubleStream.of(consumptionFromGridPerMonth())
                           .boxed()
                           .map(String::valueOf)
                           .collect(Collectors.joining(", "));
    }

    private double[] consumptionFromGridPerMonth() {
        HashMap<YearMonth, Double> resultMap = new HashMap<>();
        for (int i = 1; i <= 12; i++) {
            resultMap.put(YearMonth.of(year.getValue(), i), 0.0);
        }
        inverter.consumptionFromGrid().entrySet().stream()
                .filter(entry -> entry.getKey().getYear() == this.year.getValue())
                .forEach(entry -> resultMap.put(entry.getKey(), entry.getValue()));
        return resultMap.entrySet().stream()
                           .sorted(Map.Entry.comparingByKey())
                           .map(Map.Entry::getValue)
                           .flatMapToDouble(DoubleStream::of)
                           .toArray();
    }

    public double consumptionKwh() {
        return DoubleStream.of(consumptionFromGridPerMonth()).sum();
    }

    public String fedInPerMonthAsString() {
        return DoubleStream.of(inverter.fedInToGrid().entrySet().stream()
                                      .sorted(Map.Entry.comparingByKey())
                                      .map(Map.Entry::getValue)
                                      .flatMapToDouble(DoubleStream::of)
                                      .toArray())
                           .boxed()
                           .map(value -> value * -1.0)
                           .map(String::valueOf)
                           .collect(Collectors.joining(", "));
    }

    public double usedKwh() {
        return battery.usedKwh();
    }

    public double fedInKwh() {
        return inverter.fedInKwh();
    }

    public double batteryCycles() {
        return battery.batteryCycles();
    }

    public double batteryLifetimeLeftPercent() {
        return 100.0 - 100.0 / battery.lifetimeCycles() * battery.batteryCycles();
    }

    public double estimatedLifetimeYears() {
        return 100 / (100.0 / battery.lifetimeCycles() * battery.batteryCycles());
    }

    public double lostFeedInMoney() {
        return fedInKwhToEnergyPurchaseAgreementPartner() * feedInTariffs.feedInTariffGrid() +
                fedInKwhToCommunity() * feedInTariffs.feedInTariffEnergyCommunity();
    }

    private double fedInKwhToEnergyPurchaseAgreementPartner() {
        return battery.usedKwh() * (1.0 - feedInTariffs.percentageAmountDeliveryToCommunity());
    }

    private double fedInKwhToCommunity() {
        return battery.usedKwh() * feedInTariffs.percentageAmountDeliveryToCommunity();
    }

    public double savedMoneyPerYear() {
        return savedMoneyBecauseOfSavedPower() - lostFeedInMoney();
    }

    public double savedMoneyBecauseOfSavedPower() {
        return inputPrice * battery.usedKwh();
    }

    public double savedMoneyPerLifetime() {
        return inputPrice * inverter.fedInKwh() * estimatedLifetimeYears();
    }

    public double remainingBatteryPower() {
        return battery.currentBatteryPower();
    }

}
