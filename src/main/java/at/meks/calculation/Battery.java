package at.meks.calculation;

import at.meks.PowerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Battery {

    private static final Logger logger = LoggerFactory.getLogger(Battery.class);

    private final double maxBatteryCapacityKwh;
    private final int lifetimeCycles;
    private double currentBatteryPower;
    private double usedKwh;
    private double fedInKwh;

    private final Map<Month, Double> consumptionFromGrid = new HashMap<>();
    private final Map<Month, Double> fedInToGrid = new HashMap<>();

    public Battery(double maxBatteryCapacityKwh, int lifetimeCycles, Collection<PowerData> powerData) {
        this.maxBatteryCapacityKwh = maxBatteryCapacityKwh;
        this.lifetimeCycles = lifetimeCycles;
        Arrays.stream(Month.values()).forEach(month -> consumptionFromGrid.put(month, 0.0));
        Arrays.stream(Month.values()).forEach(month -> fedInToGrid.put(month, 0.0));
        powerData.forEach(this::add);
    }

    private void add(PowerData powerData) {
        consume(powerData.timestampUntil(), powerData.consumptionKwh());
        save(powerData.timestampUntil(), powerData.fedInKwh());
    }

    private void consume(LocalDateTime timestamp, double kwh) {
        double removedPower = Math.min(kwh, currentBatteryPower);
        currentBatteryPower -= removedPower;
        usedKwh += removedPower;
        double consumedFromGrid = kwh - removedPower;
        consumptionFromGrid.compute(timestamp.getMonth(), (month, currentConsumption) -> currentConsumption == null ? consumedFromGrid : currentConsumption + consumedFromGrid);
        logger.debug("consumed kwh {}; currentCapacity: {}; usedKwh: {}", kwh, currentBatteryPower, usedKwh);
    }

    private void save(LocalDateTime timestamp, double kwh) {
        double maxSavableKwh = maxBatteryCapacityKwh - currentBatteryPower;
        double fedInKwh = 0.0;
        double savedKwh = kwh;
        if (kwh > maxSavableKwh) {
            fedInKwh = kwh - maxSavableKwh;
            savedKwh = maxSavableKwh;
        }
        currentBatteryPower = currentBatteryPower + savedKwh;
        this.fedInKwh += fedInKwh;
        final double furtherFedInForMonth = fedInKwh;
        fedInToGrid.compute(timestamp.getMonth(), (month, currentValue) -> currentValue == null ? furtherFedInForMonth : currentValue + furtherFedInForMonth);
        logger.debug("saved kwh {}; currentCapacity: {}; usedKwh: {}", kwh, currentBatteryPower, usedKwh);
    }

    public double usedKwh() {
        return usedKwh;
    }

    public double batteryCycles() {
        return usedKwh / maxBatteryCapacityKwh;
    }

    public double fedInKwh() {
        return fedInKwh;
    }

    public double maxBatteryCapacityKwh() {
        return maxBatteryCapacityKwh;
    }

    public int lifetimeCycles() {
        return lifetimeCycles;
    }

    public Map<Month, Double> consumptionFromGrid() {
        return consumptionFromGrid;
    }

    public Map<Month, Double> fedInToGrid() {
        return fedInToGrid;
    }


    public double currentBatteryPower() {
        return currentBatteryPower;
    }
}
