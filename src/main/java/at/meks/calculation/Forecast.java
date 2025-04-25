package at.meks.calculation;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public record Forecast(double usedKwh, double fedInKwh,
                       double batteryCycles,
                       double batteryLifetimeLeftPercent, double estimatedLifetimeYears, double savedMoneyPerYear,
                       double savedMoneyPerLifetime, double[] consumptionFromGridPerMonth, double[] fedInPerMonth,
                       double remainingBatteryPower) {

    public Forecast(Battery battery, double inputPrice) {
        this(battery.usedKwh(),
             battery.fedInKwh(),
             battery.usedKwh() / battery.maxBatteryCapacityKwh(),
             100.0 - 100.0 / battery.lifetimeCycles() * battery.batteryCycles(),
             estimatedLifetime(battery),
             inputPrice * battery.usedKwh(),
             inputPrice * battery.fedInKwh() * estimatedLifetime(battery),
             battery.consumptionFromGrid().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(Map.Entry::getValue)
                    .flatMapToDouble(DoubleStream::of)
                    .toArray(),
             battery.fedInToGrid().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(Map.Entry::getValue)
                    .flatMapToDouble(DoubleStream::of)
                    .toArray(),
             battery.currentBatteryPower());
    }

    private static double estimatedLifetime(Battery battery) {
        return 100 / (100.0 / battery.lifetimeCycles() * battery.batteryCycles());
    }

    public String consumptionFromGridPerMonthAsString() {
        return DoubleStream.of(consumptionFromGridPerMonth)
                           .boxed()
                           .map(String::valueOf)
                           .collect(Collectors.joining(", "));
    }

    public String fedInPerMonthAsString() {
        return DoubleStream.of(fedInPerMonth)
                           .boxed()
                           .map(value -> value * -1.0)
                           .map(String::valueOf)
                           .collect(Collectors.joining(", "));
    }

    public double consumptionKwh() {
        return DoubleStream.of(consumptionFromGridPerMonth).sum();
    }
}
