package at.meks;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class PowerDataRepo {

    private final Set<PowerData> dataList = new HashSet<>();

    public void add(PowerData powerData) {
        dataList.add(powerData);
    }

    public List<PowerData> asList(int year) {
        return dataList.stream()
                       .filter(powerData -> powerData.timestampUntil().minusSeconds(1).getYear() == year)
                       .sorted(Comparator.comparing(PowerData::timestampUntil))
                       .toList();
    }

    public Collection<Integer> years() {
        return dataList.stream()
                       .map(powerData -> powerData.timestampUntil().minusSeconds(1).getYear())
                       .distinct()
                       .toList();
    }
}
