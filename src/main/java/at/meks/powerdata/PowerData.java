package at.meks.powerdata;

import java.time.Year;
import java.util.List;
import java.util.stream.Stream;

public class PowerData {

    private final List<SinglePowerData> powerData;

    public PowerData(List<SinglePowerData> powerData) {
        this.powerData = powerData;
    }

    public Stream<SinglePowerData> stream(Year year) {
        return powerData.stream()
                        .filter(data -> data.timestampUntil().minusSeconds(1).getYear() == year.getValue());
    }

    public Stream<SinglePowerData> stream() {
        return powerData.stream();
    }
}
