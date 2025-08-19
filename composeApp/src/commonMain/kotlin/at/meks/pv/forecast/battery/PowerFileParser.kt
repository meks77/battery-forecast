package at.meks.pv.forecast.battery.powerdataupload

//import org.apache.commons.csv.CSVFormat
//import org.apache.commons.csv.CSVRecord
//import java.io.Reader
//import java.text.NumberFormat
//import java.text.ParseException
//import java.util.*
//import java.util.stream.Stream

class PowerFileParser {
    // TODO: reimplement with pure kotlin code

//    val columnHeaderTimestamp = "timestamp"
//    val columnHeaderPower = "power"
//    val dateTimeFormat = LocalDateTime.Format {
//        day()
//        char('.')
//        monthNumber()
//        char('.')
//        year()
//        char(' ')
//        hour()
//        char(':')
//        minute()
//    }
//    val numberFormat = NumberFormat.getNumberInstance(Locale.GERMAN)
//
//    private val fedInRecords: Iterable<CSVRecord>
//    private val consumptionRecords: Iterable<CSVRecord>
//
//    constructor(fedInReader: Reader, consumptionReader: Reader) {
//        fedInRecords = CSVFormat.RFC4180.builder()
//            .setDelimiter(";")
//            .setHeader(columnHeaderTimestamp, columnHeaderPower)
//            .setSkipHeaderRecord(true)
//            .get()
//            .parse(fedInReader)
//        consumptionRecords = CSVFormat.RFC4180.builder()
//            .setDelimiter(";")
//            .setHeader(columnHeaderTimestamp, columnHeaderPower)
//            .setSkipHeaderRecord(true)
//            .get()
//            .parse(consumptionReader)
//    }
//
//    fun stream(): Stream<SinglePowerData> {
//        val fedInIterator = fedInRecords.iterator()
//        val consumptionIterator = consumptionRecords.iterator()
//
//        var currentFedInRecord: CSVRecord? = fedInIterator.next()
//        var currentConsumptionRecord: CSVRecord? = consumptionIterator.next()
//
//        val powerData = mutableListOf<SinglePowerData>()
//        while (currentFedInRecord != null || currentConsumptionRecord != null) {
//            val currentFedInTimestamp = getTimestampOrNull(currentFedInRecord)
//            val currentConsumptionTimestamp = getTimestampOrNull(currentConsumptionRecord)
//            if (currentFedInTimestamp == currentConsumptionTimestamp) {
//                powerData.add(getPowerData(currentFedInRecord!!, currentConsumptionRecord!!))
//                currentFedInRecord = nextRecord(fedInIterator)
//                currentConsumptionRecord = nextRecord(consumptionIterator)
//            } else if (fedInIsBeforeConsumption(currentFedInTimestamp, currentConsumptionTimestamp)
//                || onlyFedInIsAvailable(currentFedInRecord, currentConsumptionRecord)) {
//                powerData.add(getPowerDataForFedInRecord(currentFedInRecord!!))
//                currentFedInRecord = nextRecord(fedInIterator)
//            } else if (consumptionIsBeforeFedIn(currentFedInTimestamp, currentConsumptionTimestamp)
//                || onlyConsumptionIsAvailable(currentFedInRecord, currentConsumptionRecord)) {
//                powerData.add(getPowerDataForConsumptionRecord(currentConsumptionRecord!!))
//                currentConsumptionRecord = nextRecord(consumptionIterator)
//            } else {
//                throw IllegalStateException("this is a bug. An unexpected Situation happened")
//            }
//        }
//
//        return powerData.stream()
//    }
//
//    private fun onlyFedInIsAvailable(currentFedInRecord: CSVRecord?,
//                                     currentConsumptionRecord: CSVRecord?): Boolean {
//        return currentFedInRecord != null && currentConsumptionRecord == null
//    }
//
//    private fun onlyConsumptionIsAvailable(currentFedInRecord: CSVRecord?,
//                                           currentConsumptionRecord: CSVRecord?): Boolean {
//        return currentFedInRecord == null && currentConsumptionRecord != null
//    }
//
//    private fun consumptionIsBeforeFedIn(currentFedInTimestamp: LocalDateTime?,
//                                         currentConsumptionTimestamp: LocalDateTime?): Boolean {
//        return currentFedInTimestamp != null && currentConsumptionTimestamp != null &&
//                currentConsumptionTimestamp < currentFedInTimestamp
//    }
//
//    private fun fedInIsBeforeConsumption(currentFedInTimestamp: LocalDateTime?,
//                                         currentConsumptionTimestamp: LocalDateTime?): Boolean {
//
//        return currentFedInTimestamp !=null && currentConsumptionTimestamp != null &&
//                currentFedInTimestamp < currentConsumptionTimestamp
//    }
//
//    private fun nextRecord(iterator: Iterator<CSVRecord>): CSVRecord? {
//        return if (iterator.hasNext()) {
//            iterator.next()
//        } else {
//            null
//        }
//    }
//
//    fun getPowerData(fedInRecord: CSVRecord, consumptionRecord: CSVRecord): SinglePowerData {
//        val fedInTimestamp = fedInRecord.get(columnHeaderTimestamp)
//        val consumptionTimestamp = consumptionRecord.get(columnHeaderTimestamp)
//        if (!fedInTimestamp.equals(consumptionTimestamp)) {
//            throw IllegalStateException(
//                "Fed-In timestamp '" + fedInTimestamp + "' and consumption timestamp '" + consumptionTimestamp
//                        + "' don't match")
//        }
//        return SinglePowerData(getTimestamp(fedInRecord),
//            getPowerKwh(fedInRecord),
//            getPowerKwh(consumptionRecord))
//    }
//
//    private fun getPowerDataForFedInRecord(fedInRecord: CSVRecord): SinglePowerData {
//        return SinglePowerData(getTimestamp(fedInRecord),
//            getPowerKwh(fedInRecord),
//            0.0)
//    }
//
//    private fun getPowerDataForConsumptionRecord(consumptionRecord: CSVRecord): SinglePowerData {
//        return SinglePowerData(getTimestamp(consumptionRecord),
//            0.0,
//            getPowerKwh(consumptionRecord))
//    }
//
//    private fun getPowerKwh(record: CSVRecord): Double {
//        try {
//            return numberFormat.parse(record.get(columnHeaderPower)).toDouble()
//        } catch (e: ParseException) {
//            throw IllegalStateException(columnHeaderPower + " couldn't be parsed", e)
//        }
//    }
//
//
//    fun getTimestampOrNull(record: CSVRecord?): LocalDateTime? {
//        return if (record == null)
//            null
//        else
//            LocalDateTime.parse(record.get(columnHeaderTimestamp), dateTimeFormat)
//    }
//
//    fun getTimestamp(record: CSVRecord): LocalDateTime {
//        return LocalDateTime.parse(record.get(columnHeaderTimestamp), dateTimeFormat)
//    }
}
