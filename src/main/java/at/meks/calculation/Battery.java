package at.meks.calculation;

import at.meks.powerdata.SinglePowerData;

public class Battery {

    private final double batteryCapacityKwh;
    private final int lifetimeCycles;
    private double currentBatteryPower;
    private double usedKwh;

    public Battery(double batteryCapacityKwh, int lifetimeCycles) {
        this.batteryCapacityKwh = batteryCapacityKwh;
        this.lifetimeCycles = lifetimeCycles;
    }

    double consume(double kwh) {
        double removedPower = Math.min(kwh, currentBatteryPower);
        currentBatteryPower -= removedPower;
        usedKwh += removedPower;
        return removedPower;
    }

    double save(double kwh) {
        double maxSavableKwh = batteryCapacityKwh - currentBatteryPower;
        double savedKwh = Math.min(maxSavableKwh, kwh);
        currentBatteryPower = currentBatteryPower + savedKwh;
        return savedKwh;
    }

    public double usedKwh() {
        return usedKwh;
    }

    public double batteryCycles() {
        return usedKwh / batteryCapacityKwh;
    }

    public int lifetimeCycles() {
        return lifetimeCycles;
    }

    public double currentBatteryPower() {
        return currentBatteryPower;
    }
}
