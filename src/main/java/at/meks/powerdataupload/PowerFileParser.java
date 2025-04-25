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
import java.util.stream.Stream;

class PowerFileParser {

    public static final String MESSZEITPUNKT = "Messzeitpunkt";
    public static final String EINSPEISUNG_KWH = "Einspeisung (kWh)";
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    public static final NumberFormat NUMBER_PARSER = NumberFormat.getNumberInstance(Locale.GERMAN);
    public static final String VERBRAUCH_KWH = "Verbrauch (kWh)";

    private final Iterable<CSVRecord> fedInRecords;
    private final Iterable<CSVRecord> consumptionRecords;

    PowerFileParser(Reader fedInReader, Reader consumptionReader) throws IOException {
        fedInRecords = CSVFormat.Builder.create()
                                        .setDelimiter(";")
                                        .setHeader(MESSZEITPUNKT, EINSPEISUNG_KWH)
                                        .get()
                                        .parse(fedInReader);
        consumptionRecords = CSVFormat.Builder.create()
                .setDelimiter(";")
                .setHeader(MESSZEITPUNKT, VERBRAUCH_KWH)
                .get()
                .parse(consumptionReader);
    }

    Stream<PowerData> stream() {
        Iterator<CSVRecord> fedInIterator = fedInRecords.iterator();
        Iterator<CSVRecord> consumptionIterator = consumptionRecords.iterator();
        fedInIterator.next();
        consumptionIterator.next();

        CSVRecord currentFedInRecord = fedInIterator.next();
        CSVRecord currentConsumptionRecord = consumptionIterator.next();

        Collection<PowerData> powerData = new ArrayList<>();

        do {
            var currentFedInTimestamp = getTimestamp(currentFedInRecord);
            var currentConsumptionTimestamp = getTimestamp(currentConsumptionRecord);
            if (currentFedInTimestamp.equals(currentConsumptionTimestamp)) {
                powerData.add(getPowerData(currentFedInRecord, currentConsumptionRecord));
                currentFedInRecord = fedInIterator.next();
                currentConsumptionRecord = consumptionIterator.next();
            } else if (currentFedInTimestamp.isBefore(currentConsumptionTimestamp)) {
                powerData.add(getPowerDataForFedInRecord(currentFedInRecord));
                currentFedInRecord = fedInIterator.next();
            } else {
                powerData.add(getPowerDataForConsumptionRecord(currentConsumptionRecord));
                currentConsumptionRecord = consumptionIterator.next();
            }
        } while (fedInIterator.hasNext() || consumptionIterator.hasNext());

        return powerData.stream();
    }

    private static PowerData getPowerData(CSVRecord fedInRecord, CSVRecord consumptionRecord) {
        String fedInTimestamp = fedInRecord.get(MESSZEITPUNKT);
        String consumptionTimestamp = consumptionRecord.get(MESSZEITPUNKT);
        if (!fedInTimestamp.equals(consumptionTimestamp)) {
            throw new IllegalStateException("Fed-In timestamp '" + fedInTimestamp + "' and consumption timestamp '" + consumptionTimestamp + "' don't match");
        }
        return new PowerData(getTimestamp(fedInRecord),
                             getPowerKwh(fedInRecord, EINSPEISUNG_KWH),
                             getPowerKwh(consumptionRecord, VERBRAUCH_KWH));
    }

    private static PowerData getPowerDataForFedInRecord(CSVRecord fedInRecord) {
        return new PowerData(getTimestamp(fedInRecord),
                             getPowerKwh(fedInRecord, EINSPEISUNG_KWH),
                             0.0);
    }

    private static PowerData getPowerDataForConsumptionRecord(CSVRecord consumptionRecord) {
        return new PowerData(getTimestamp(consumptionRecord),
                             0.0,
                             getPowerKwh(consumptionRecord, VERBRAUCH_KWH));
    }

    private static double getPowerKwh(CSVRecord record, String columnHeader) {
        try {
            return NUMBER_PARSER.parse(record.get(columnHeader)).doubleValue();
        } catch (ParseException e) {
            throw new IllegalStateException(columnHeader + " couldn't be parsed", e);
        }
    }

    private static LocalDateTime getTimestamp(CSVRecord record) {
        return LocalDateTime.parse(record.get(MESSZEITPUNKT), DATE_TIME_FORMATTER);
    }
}
