package at.meks.calculation;

import at.meks.powerdata.SinglePowerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

public class Battery {

    private static final Logger logger = LoggerFactory.getLogger(Battery.class);

    private final double batteryCapacityKwh;
    private final int lifetimeCycles;
    private double currentBatteryPower;
    private double usedKwh;
    private double fedInKwh;

    private final Map<YearMonth, Double> consumptionFromGrid = new HashMap<>();
    private final Map<YearMonth, Double> fedInToGrid = new HashMap<>();

    public Battery(double batteryCapacityKwh, int lifetimeCycles) {
        this.batteryCapacityKwh = batteryCapacityKwh;
        this.lifetimeCycles = lifetimeCycles;
    }

    public void add(SinglePowerData singlePowerData) {
        consume(singlePowerData.timestampUntil(), singlePowerData.consumptionKwh());
        save(singlePowerData.timestampUntil(), singlePowerData.fedInKwh());
    }

    private void consume(LocalDateTime timestamp, double kwh) {
        double removedPower = Math.min(kwh, currentBatteryPower);
        currentBatteryPower -= removedPower;
        usedKwh += removedPower;
        double consumedFromGrid = kwh - removedPower;
        consumptionFromGrid.compute(YearMonth.of(timestamp.getYear(), timestamp.getMonth()),
                                    (month, currentConsumption) ->
                                            currentConsumption == null ? consumedFromGrid :
                                                    currentConsumption + consumedFromGrid);
        logger.debug("consumed kwh {}; currentCapacity: {}; usedKwh: {}", kwh, currentBatteryPower, usedKwh);
    }

    private void save(LocalDateTime timestamp, double kwh) {
        double maxSavableKwh = batteryCapacityKwh - currentBatteryPower;
        double fedInKwh = 0.0;
        double savedKwh = kwh;
        if (kwh > maxSavableKwh) {
            fedInKwh = kwh - maxSavableKwh;
            savedKwh = maxSavableKwh;
        }
        currentBatteryPower = currentBatteryPower + savedKwh;
        this.fedInKwh += fedInKwh;
        final double furtherFedInForMonth = fedInKwh;
        fedInToGrid.compute(YearMonth.of(timestamp.getYear(), timestamp.getMonth()),
                            (month, currentValue) ->
                                    currentValue == null ? furtherFedInForMonth : currentValue + furtherFedInForMonth);
        logger.debug("saved kwh {}; currentCapacity: {}; usedKwh: {}", kwh, currentBatteryPower, usedKwh);
    }

    public double usedKwh() {
        return usedKwh;
    }

    public double batteryCycles() {
        return usedKwh / batteryCapacityKwh;
    }

    public double fedInKwh() {
        return fedInKwh;
    }

    public int lifetimeCycles() {
        return lifetimeCycles;
    }

    public Map<YearMonth, Double> consumptionFromGrid() {
        return consumptionFromGrid;
    }

    public Map<YearMonth, Double> fedInToGrid() {
        return fedInToGrid;
    }

    public double currentBatteryPower() {
        return currentBatteryPower;
    }
}
