package at.meks.pv.forecast.battery.import

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test

class PowerfileParserTest {

    @Test
    fun `simple Netz NOE file`() {
        val fileContent =
            """
            Messzeitpunkt;Verbrauch (kWh)
            31.12.2024 23:59;5,3
            01.01.2025 00:00;4,7
            """

        val result = PowerFileParser(fileContent).stream()

        val iterator = result.iterator()
        iterator.next().shouldBe(PowerLine(LocalDateTime(2024, 12, 31, 23, 59), 5.3))
        iterator.next().shouldBe(PowerLine(LocalDateTime(2025, 1, 1, 0, 0), 4.7))
    }

    @Test
    fun `without header`() {
        val fileContent =
            """
            31.12.2024 23:59;5,3
            01.01.2025 00:00;4,7
            """

        val result = PowerFileParser(fileContent, FileContentStructure(containsHeader = false)).stream()

        val iterator = result.iterator()
        iterator.next().shouldBe(PowerLine(LocalDateTime(2024, 12, 31, 23, 59), 5.3))
        iterator.next().shouldBe(PowerLine(LocalDateTime(2025, 1, 1, 0, 0), 4.7))
    }

    @Test
    fun `differnt column order`() {
        val fileContent =
            """
            col1;Messzeitpunkt;Verbrauch (kWh);last col
            x;5,3;31.12.2024 23:59;y
            a;4,7;01.01.2025 00:00;b
            """

        val result =
            PowerFileParser(fileContent, FileContentStructure(colIndexTimestamp = 2, colIndexPower = 1)).stream()

        val iterator = result.iterator()
        iterator.next().shouldBe(PowerLine(LocalDateTime(2024, 12, 31, 23, 59), 5.3))
        iterator.next().shouldBe(PowerLine(LocalDateTime(2025, 1, 1, 0, 0), 4.7))
    }

    @Test
    fun `custom column separator`() {
        val fileContent =
            """
            Messzeitpunkt;Verbrauch (kWh)
            31.12.2024 23:59|5,3
            01.01.2025 00:00|4,7
            """

        val result = PowerFileParser(fileContent, FileContentStructure(columnSeparator = '|')).stream()

        val iterator = result.iterator()
        iterator.next().shouldBe(PowerLine(LocalDateTime(2024, 12, 31, 23, 59), 5.3))
        iterator.next().shouldBe(PowerLine(LocalDateTime(2025, 1, 1, 0, 0), 4.7))
    }

    @Test
    fun `column index out of range`() {
        val fileContent =
            """
            31.12.2024 23:59;5,3
            01.01.2025 00:00;4,7
            """

        shouldThrow<IndexOutOfBoundsException> {
            PowerFileParser(fileContent, FileContentStructure(colIndexTimestamp =  2)).stream()
        }

    }

    @Test
    fun `only header line available`() {
        val fileContent =
            """
            Messzeitpunkt;Verbrauch (kWh)
            """

        val result = PowerFileParser(fileContent).stream()

        result.count() shouldBe 0
    }

    @Test
    fun `empty file not expecting header`() {
        val fileContent =
            """
            """

        val result = PowerFileParser(fileContent, FileContentStructure(containsHeader = false)).stream()

        result.count() shouldBe 0
    }

    @Test
    fun `empty file expecting header`() {
        val fileContent =
            """
            """

        val result = PowerFileParser(fileContent).stream()

        result.count() shouldBe 0
    }

    @Test
    fun `incorrect timestamp format`() {
        val fileContent = """
                    Messzeitpunkt;Verbrauch (kWh)
                    31-12-2024 23:59;5,5
                    """


        shouldThrow<IllegalTimestampFormatException> {
            PowerFileParser(fileContent, FileContentStructure()).stream()
        }
    }

    @Test
    fun `incorrect power value format`() {
        val fileContent = """
                    Messzeitpunkt;Verbrauch (kWh)
                    31.12.2024 23:59;5%5
                    """

        shouldThrow<IllegalPowerValueFormatException> {
            PowerFileParser(fileContent, FileContentStructure()).stream()
        }
    }


}