package at.meks.powerdataupload;

import at.meks.powerdata.SinglePowerData;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class PowerFileParserTest {

    @Test
    void resultContainsExpectedData() throws IOException {
        var parser = new PowerFileParser(readerFor("fed-in-tiny.csv"), readerFor("consumption-tiny.csv"));

        assertThat(parser.stream())
                .containsExactlyInAnyOrder(
                        new SinglePowerData(LocalDateTime.of(2022, 9, 5, 8, 0), 1.123, 0.0),
                        new SinglePowerData(LocalDateTime.of(2022, 10, 21, 0, 15), 0.0, 0.046),
                        new SinglePowerData(LocalDateTime.of(2022, 10, 21, 0, 30), 0.0, 0.043),
                        new SinglePowerData(LocalDateTime.of(2022, 10, 21, 0, 45), 0.0, 0.043),
                        new SinglePowerData(LocalDateTime.of(2022, 10, 21, 1, 0), 0.0, 0.048),
                        new SinglePowerData(LocalDateTime.of(2022, 11, 12, 6, 45), 0.0, 0.046),
                        new SinglePowerData(LocalDateTime.of(2022, 12, 19, 14, 0), 4.85, 0.0),
                        new SinglePowerData(LocalDateTime.of(2022, 12, 20, 6, 45), 25.345, 0.0),
                        new SinglePowerData(LocalDateTime.of(2022, 12, 20, 7, 0), 98345.987, 0.066),
                        new SinglePowerData(LocalDateTime.of(2022, 12, 20, 7, 15), 0.48, 0.0),
                        new SinglePowerData(LocalDateTime.of(2022, 12, 23, 3, 30), 0.52, 0.0),
                        new SinglePowerData(LocalDateTime.of(2023, 12, 20, 7, 0), 0.0, 0.066)
                );
    }

    private Reader readerFor(String filename) {
        return new InputStreamReader(inputStreamFor(filename));
    }

    private InputStream inputStreamFor(String filename) {
        return Objects.requireNonNull(
                getClass().getResourceAsStream("/powerdataparser/" + filename));
    }

    @Test
    void parsingOneYearEndsSuccessfully() {
        assertThatNoException()
                .isThrownBy(() -> new PowerFileParser(readerFor("fed-in_one_year.csv"),
                                                      readerFor("consumption_one_year.csv")));
    }
}