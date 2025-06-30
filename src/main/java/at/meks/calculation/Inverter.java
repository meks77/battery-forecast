package at.meks.calculation;

import at.meks.powerdata.SinglePowerData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

public class Inverter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Battery battery;

    private double fedInKwh;

    private final Map<YearMonth, Double> consumptionFromGrid = new HashMap<>();
    private final Map<YearMonth, Double> fedInToGrid = new HashMap<>();

    public Inverter(Battery battery) {
        this.battery = battery;
    }

    public Map<YearMonth, Double> consumptionFromGrid() {
        return consumptionFromGrid;
    }

    public Map<YearMonth, Double> fedInToGrid() {
        return fedInToGrid;
    }

    public double fedInKwh() {
        return fedInKwh;
    }

    public void add(SinglePowerData singlePowerData) {
        consume(singlePowerData.timestampUntil(), singlePowerData.consumptionKwh());
        save(singlePowerData.timestampUntil(), singlePowerData.fedInKwh());
    }

    private void consume(LocalDateTime timestamp, double kwh) {
        double consumedFromBattery = battery.consume(kwh);
        double consumedFromGrid = kwh - consumedFromBattery;
        consumptionFromGrid.compute(YearMonth.of(timestamp.getYear(), timestamp.getMonth()),
                                    (month, currentConsumption) ->
                                            currentConsumption == null ? consumedFromGrid :
                                                    currentConsumption + consumedFromGrid);
        logger.debug("consumed kwh {}; currentCapacity: {}; usedKwh: {}", kwh, battery.currentBatteryPower(), battery.usedKwh());
    }

    private void save(LocalDateTime timestamp, double kwh) {
        double savedKwh = battery.save(kwh);
        double fedInKwh = 0.0;
        if (savedKwh < kwh) {
            fedInKwh = kwh - savedKwh;
        }
        this.fedInKwh += fedInKwh;
        final double furtherFedInForMonth = fedInKwh;
        fedInToGrid.compute(YearMonth.of(timestamp.getYear(), timestamp.getMonth()),
                            (month, currentValue) ->
                                    currentValue == null ? furtherFedInForMonth : currentValue + furtherFedInForMonth);
        logger.debug("saved kwh {}; currentCapacity: {}; usedKwh: {}", kwh, battery.currentBatteryPower(), battery.usedKwh());
    }
}
