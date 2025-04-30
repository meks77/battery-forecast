package at.meks.powerdata;

import jakarta.enterprise.context.ApplicationScoped;

import java.time.Year;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class PowerDataRepo {

    private final Set<SinglePowerData> dataList = new HashSet<>();

    public void add(SinglePowerData singlePowerData) {
        dataList.add(singlePowerData);
    }

    public PowerData powerData(Year year) {
        return new PowerData(dataList.stream()
                       .filter(powerData -> powerData.timestampUntil().minusSeconds(1).getYear() == year.getValue())
                       .sorted(Comparator.comparing(SinglePowerData::timestampUntil))
                       .toList());
    }

    public Collection<Integer> years() {
        return dataList.stream()
                       .map(powerData -> powerData.timestampUntil().minusSeconds(1).getYear())
                       .distinct()
                       .toList();
    }
}
