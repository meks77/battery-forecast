package at.meks.pv.forecast.battery.import

import at.meks.pv.forecast.battery.PowerDataRepo
import at.meks.pv.forecast.battery.RuntimeContext.Companion.currentContext
import at.meks.pv.forecast.battery.createLogger
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.char


data class PowerLine(val timestamp: LocalDateTime, val power: Double)

class PowerfileImporter {


    val logger = createLogger(this)

    fun import(fileContent: String, type: PowerDataRepo.PowerType) {
        logger.info("import file content of type $type")
        val parser = PowerFileParser(fileContent)
        parser.stream().forEach {
            logger.info("power data: $it")
            currentContext().powerDataRepo().addOrReplace(it.timestamp, it.power, type)
        }
    }

}

data class FileContentStructure(val columnSeparator: Char = ';', val colIndexTimestamp: Int = 0,
                                val colIndexPower: Int = 1, val containsHeader: Boolean = true)

class IllegalTimestampFormatException(message: String) : Exception(message) { }

class IllegalPowerValueFormatException(message: String) : Exception(message) { }


class PowerFileParser(val fileContent: String,
                      val structure: FileContentStructure = FileContentStructure()) {

    private val logger = createLogger(this)

    val dateTimeFormat = LocalDateTime.Format {
        day()
        char('.')
        monthNumber()
        char('.')
        year()
        char(' ')
        hour()
        char(':')
        minute()
    }

    fun stream(): Sequence<PowerLine> {
        val lines = lines()
        if (lines.isEmpty()) {
            return emptySequence()
        }
        return lines
            .subList(firstDataLine(), lines.size)
            .map { it.split(structure.columnSeparator) }
            .map { toSinglePowerData(it) }
            .asSequence()
    }

    private fun firstDataLine(): Int = if (structure.containsHeader) 1 else 0

    private fun toSinglePowerData(rawLineContent: List<String>): PowerLine {
        val timestampUntil = toTimestamp(rawLineContent, structure.colIndexTimestamp)
        val powerValue = toPowerValue(rawLineContent, structure.colIndexPower)
        return PowerLine(timestampUntil, powerValue)
    }

    private fun toPowerValue(rawLineContent: List<String>,
                             columnIndex: Int): Double {
        try {
            return toDouble(rawLineContent[columnIndex])
        } catch (e: NumberFormatException) {
            logger.error("Couldn't parse power value ${rawLineContent[columnIndex]} as double", e)
            throw IllegalPowerValueFormatException("Couldn't parse power value ${rawLineContent[columnIndex]}. Value should look like 5.8")
        }
    }

    private fun toDouble(rawvalue: String): Double {
        val value = rawvalue.trim()
        if (value.contains('.') && value.contains(',')) {
            if (value.indexOf('.') < value.indexOf(',')) {
                return value.replace(".", "").replace(',', '.').toDouble()
            } else {
                return value.replace(",", "").replace('.', ',').toDouble()
            }
        } else if (value.contains(',')) {
            return value.replace(",", ".").toDouble()
        } else {
            return value.toDouble()
        }
    }

    private fun toTimestamp(rawLineContent: List<String>,
                            columnIndex: Int): LocalDateTime {
        val rawTimestamp = rawLineContent[columnIndex].trim()
        try {
            val timestampUntil = LocalDateTime.parse(rawTimestamp, dateTimeFormat)
            return timestampUntil
        } catch (e: IllegalArgumentException) {
            logger.error("Couldn't parse $rawTimestamp", e)
            throw IllegalTimestampFormatException("Couldn't parse $rawTimestamp as  timestamp.")
        }
    }

    private fun lines(): List<String> {
        return fileContent.split("\n")
            .filter { it.isNotBlank() }
    }
}