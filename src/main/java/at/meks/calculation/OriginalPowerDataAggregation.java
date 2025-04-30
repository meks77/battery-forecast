package at.meks.calculation;

import at.meks.powerdata.PowerData;
import at.meks.powerdata.SinglePowerData;

import java.time.Month;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class OriginalPowerDataAggregation {

    private final Map<Month, Double> consumptionFromGrid = new HashMap<>();
    private final Map<Month, Double> fedInToGrid = new HashMap<>();
    private final Collection<Integer> years;

    public OriginalPowerDataAggregation(PowerData powerData, Collection<Integer> years) {
        this.years = years;
        Arrays.stream(Month.values()).forEach(month -> consumptionFromGrid.put(month, 0.0));
        Arrays.stream(Month.values()).forEach(month -> fedInToGrid.put(month, 0.0));
        powerData.stream().forEach(this::calculatePowerStatistics);
    }

    private void calculatePowerStatistics(SinglePowerData singlePowerData) {
        consumptionFromGrid.compute(singlePowerData.timestampUntil().getMonth(),
                                    (month, currentConsumption) ->
                                            currentConsumption == null ? singlePowerData.consumptionKwh() :
                                                    currentConsumption + singlePowerData.consumptionKwh());
        fedInToGrid.compute(singlePowerData.timestampUntil().getMonth(),
                            (month, currentValue) ->
                                    currentValue == null ? singlePowerData.fedInKwh() : currentValue + singlePowerData.fedInKwh());
    }

    public String consumptionPerMonthAsString() {
        return consumptionFromGrid.entrySet().stream()
                                  .sorted(Map.Entry.comparingByKey())
                                  .map(Map.Entry::getValue)
                                  .map(String::valueOf)
                                  .collect(Collectors.joining(", "));
    }

    public String fedInPerMonthAsString() {
        return fedInToGrid.entrySet().stream()
                          .sorted(Map.Entry.comparingByKey())
                          .map(Map.Entry::getValue)
                          .map(value -> value * -1.0)
                          .map(String::valueOf)
                          .collect(Collectors.joining(", "));
    }

    public double consumptionOfYear() {
        return consumptionFromGrid.values().stream().flatMapToDouble(DoubleStream::of).sum();
    }

    public double fedInOfYear() {
        return fedInToGrid.values().stream().flatMapToDouble(DoubleStream::of).sum();
    }

    public Integer[] years() {
        return years.stream().sorted(Comparator.reverseOrder()).toArray(Integer[]::new);
    }
}
