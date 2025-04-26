package at.meks.powerdataupload;

import at.meks.PowerData;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

class PowerFileParser {

    public static final String TIMESTAMP = "timestamp";
    public static final String POWER = "power";
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    public static final NumberFormat NUMBER_PARSER = NumberFormat.getNumberInstance(Locale.GERMAN);

    private final Iterable<CSVRecord> fedInRecords;
    private final Iterable<CSVRecord> consumptionRecords;

    PowerFileParser(Reader fedInReader, Reader consumptionReader) throws IOException {
        fedInRecords = CSVFormat.RFC4180.builder()
                                        .setDelimiter(";")
                                        .setHeader(TIMESTAMP, POWER)
                                        .setSkipHeaderRecord(true)
                                        .get()
                                        .parse(fedInReader);
        consumptionRecords = CSVFormat.RFC4180.builder()
                                              .setDelimiter(";")
                                              .setHeader(TIMESTAMP, POWER)
                                              .setSkipHeaderRecord(true)
                                              .get()
                                              .parse(consumptionReader);
    }

    Stream<PowerData> stream() {
        Iterator<CSVRecord> fedInIterator = fedInRecords.iterator();
        Iterator<CSVRecord> consumptionIterator = consumptionRecords.iterator();

        Optional<CSVRecord> currentFedInRecord = Optional.of(fedInIterator.next());
        Optional<CSVRecord> currentConsumptionRecord = Optional.of(consumptionIterator.next());

        Collection<PowerData> powerData = new ArrayList<>();
        while (currentFedInRecord.isPresent() || currentConsumptionRecord.isPresent()) {
            var currentFedInTimestamp = currentFedInRecord.map(PowerFileParser::getTimestamp);
            var currentConsumptionTimestamp = currentConsumptionRecord.map(PowerFileParser::getTimestamp);
            if (currentFedInTimestamp.isPresent() && currentFedInTimestamp.equals(currentConsumptionTimestamp)) {
                //noinspection OptionalGetWithoutIsPresent -> currentFedInTimestamp.equals in the if condition implicitly verifies if it is present
                powerData.add(getPowerData(currentFedInRecord.get(), currentConsumptionRecord.get()));
                currentFedInRecord = nextRecord(fedInIterator);
                currentConsumptionRecord = nextRecord(consumptionIterator);
            } else if (fedInIsBeforeConsumption(currentFedInTimestamp, currentConsumptionTimestamp)
                    || onlyFedInIsAvailable(currentFedInRecord, currentConsumptionRecord)) {
                //noinspection OptionalGetWithoutIsPresent -> it is verified in the if condition in the method call
                powerData.add(getPowerDataForFedInRecord(currentFedInRecord.get()));
                currentFedInRecord = nextRecord(fedInIterator);
            } else if (consumptionIsBeforeFedIn(currentFedInTimestamp, currentConsumptionTimestamp)
                    || onlyConsumptionIsAvailable(currentFedInRecord, currentConsumptionRecord)) {
                //noinspection OptionalGetWithoutIsPresent -> it is verified in the if condition in the method call
                powerData.add(getPowerDataForConsumptionRecord(currentConsumptionRecord.get()));
                currentConsumptionRecord = nextRecord(consumptionIterator);
            } else {
                throw new IllegalStateException("this is a bug. An unexpected Situation happened");
            }
        }

        return powerData.stream();
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private boolean onlyFedInIsAvailable(Optional<CSVRecord> currentFedInRecord,
                                         Optional<CSVRecord> currentConsumptionRecord) {
        return currentFedInRecord.isPresent() && currentConsumptionRecord.isEmpty();
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static boolean onlyConsumptionIsAvailable(Optional<CSVRecord> currentFedInRecord,
                                                      Optional<CSVRecord> currentConsumptionRecord) {
        return currentFedInRecord.isEmpty() && currentConsumptionRecord.isPresent();
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static boolean consumptionIsBeforeFedIn(Optional<LocalDateTime> currentFedInTimestamp,
                                                    Optional<LocalDateTime> currentConsumptionTimestamp) {
        return currentFedInTimestamp.isPresent() && currentConsumptionTimestamp.isPresent()
                && currentFedInTimestamp.get().isAfter(currentConsumptionTimestamp.get());
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static boolean fedInIsBeforeConsumption(Optional<LocalDateTime> currentFedInTimestamp,
                                                    Optional<LocalDateTime> currentConsumptionTimestamp) {
        return currentFedInTimestamp.isPresent() && currentConsumptionTimestamp.isPresent()
                && currentFedInTimestamp.get().isBefore(currentConsumptionTimestamp.get());
    }

    private static Optional<CSVRecord> nextRecord(Iterator<CSVRecord> iterator) {
        if (iterator.hasNext()) {
            return Optional.of(iterator.next());
        } else {
            return Optional.empty();
        }
    }

    private static PowerData getPowerData(CSVRecord fedInRecord, CSVRecord consumptionRecord) {
        String fedInTimestamp = fedInRecord.get(TIMESTAMP);
        String consumptionTimestamp = consumptionRecord.get(TIMESTAMP);
        if (!fedInTimestamp.equals(consumptionTimestamp)) {
            throw new IllegalStateException(
                    "Fed-In timestamp '" + fedInTimestamp + "' and consumption timestamp '" + consumptionTimestamp
                            + "' don't match");
        }
        return new PowerData(getTimestamp(fedInRecord),
                             getPowerKwh(fedInRecord),
                             getPowerKwh(consumptionRecord));
    }

    private static PowerData getPowerDataForFedInRecord(CSVRecord fedInRecord) {
        return new PowerData(getTimestamp(fedInRecord),
                             getPowerKwh(fedInRecord),
                             0.0);
    }

    private static PowerData getPowerDataForConsumptionRecord(CSVRecord consumptionRecord) {
        return new PowerData(getTimestamp(consumptionRecord),
                             0.0,
                             getPowerKwh(consumptionRecord));
    }

    private static double getPowerKwh(CSVRecord record) {
        try {
            return NUMBER_PARSER.parse(record.get(PowerFileParser.POWER)).doubleValue();
        } catch (ParseException e) {
            throw new IllegalStateException(PowerFileParser.POWER + " couldn't be parsed", e);
        }
    }

    private static LocalDateTime getTimestamp(CSVRecord record) {
        return LocalDateTime.parse(record.get(TIMESTAMP), DATE_TIME_FORMATTER);
    }
}
