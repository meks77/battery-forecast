package at.meks.pv.forecast.battery

import at.meks.pv.forecast.battery.powerdataupload.PowerFileParser
import kotlin.test.Test

class PowerFileParserTest {

    // TODO: implement Test for PowerFileParser

    @Test
    fun resultContainsExpectedData() {

//        val parser = PowerFileParser(readerFor("fed-in-tiny.csv"), readerFor("consumption-tiny.csv"))
//
//        assertThat(parser.stream())
//            .containsExactlyInAnyOrder(
//                SinglePowerData(LocalDateTime(2022, 9, 5, 8, 0), 1.123, 0.0),
//                SinglePowerData(LocalDateTime(2022, 10, 21, 0, 15), 0.0, 0.046),
//                SinglePowerData(LocalDateTime(2022, 10, 21, 0, 30), 0.0, 0.043),
//                SinglePowerData(LocalDateTime(2022, 10, 21, 0, 45), 0.0, 0.043),
//                SinglePowerData(LocalDateTime(2022, 10, 21, 1, 0), 0.0, 0.048),
//                SinglePowerData(LocalDateTime(2022, 11, 12, 6, 45), 0.0, 0.046),
//                SinglePowerData(LocalDateTime(2022, 12, 19, 14, 0), 4.85, 0.0),
//                SinglePowerData(LocalDateTime(2022, 12, 20, 6, 45), 25.345, 0.0),
//                SinglePowerData(LocalDateTime(2022, 12, 20, 7, 0), 98345.987, 0.066),
//                SinglePowerData(LocalDateTime(2022, 12, 20, 7, 15), 0.48, 0.0),
//                SinglePowerData(LocalDateTime(2022, 12, 23, 3, 30), 0.52, 0.0),
//                SinglePowerData(LocalDateTime(2023, 12, 20, 7, 0), 0.0, 0.066)
//            )
    }


//
//    private fun inputStreamFor(filename: String): InputStream {
//        return Objects.requireNonNull(
//            this.javaClass.getResourceAsStream("/powerdataparser/$filename"))
//    }

    @Test
    fun parsingOneYearEndsSuccessfully() {
//        assertThatNoException()
//            .isThrownBy { ->
//                PowerFileParser(readerFor("fed-in_one_year.csv"),
//                    readerFor("consumption_one_year.csv"))
//            }
    }
}