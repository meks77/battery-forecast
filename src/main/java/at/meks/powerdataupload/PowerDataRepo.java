package at.meks.powerdataupload;

import at.meks.PowerData;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class PowerDataRepo {

    Set<PowerData> dataList = new HashSet<>();

    public void add(PowerData powerData) {
        dataList.add(powerData);
    }

    public List<PowerData> asList(int year) {
        return dataList.stream()
                       .filter(powerData -> powerData.timestamp().getYear() == year)
                       .sorted(Comparator.comparing(PowerData::timestamp))
                       .toList();
    }

    public List<PowerData> asList() {
        return dataList.stream()
                       .sorted(Comparator.comparing(PowerData::timestamp))
                       .toList();
    }

    public Collection<Integer> years() {
        return dataList.stream()
                       .map(powerData -> powerData.timestamp().getYear())
                       .distinct()
                       .toList();
    }
}
