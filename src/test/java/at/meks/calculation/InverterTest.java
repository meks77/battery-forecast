package at.meks.calculation;

import at.meks.powerdata.SinglePowerData;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class InverterTest {

    public static final YearMonth JANUARY = YearMonth.of(2025, 1);
    private static final YearMonth FEBRUARY = YearMonth.of(2025, 2);
    private static final YearMonth MARCH = YearMonth.of(2025, 3);

    @Nested
    class FedIn {

        @Test
        void withinOneMonth() {
            var inverter = new Inverter(new Battery(10.0, 6000));
            inverter.add(new SinglePowerData(LocalDateTime.of(2025, 1, 1, 0, 15), 1.0, 0.8));
            assertThatNothingWasFedId(inverter, JANUARY);

            inverter.add(new SinglePowerData(LocalDateTime.of(2025, 1, 1, 0, 30), 3.0, 0.8));
            assertThatNothingWasFedId(inverter, JANUARY);

            inverter.add(new SinglePowerData(LocalDateTime.of(2025, 1, 1, 0, 45), 8.432, 0.8));
            assertFedIn(inverter, JANUARY, 0.832);

            inverter.add(new SinglePowerData(LocalDateTime.of(2025, 1, 1, 1, 0), 10.0, 0.8));
            assertFedIn(inverter, JANUARY, 10.032);

            inverter.add(new SinglePowerData(LocalDateTime.of(2025, 1, 2, 0, 0), 10.0, 0.8));
            assertFedIn(inverter, JANUARY, 19.232);
        }

        @Test
        void differentMonths() {
            var inverter = new Inverter(new Battery(10.0, 6000));

            inverter.add(new SinglePowerData(LocalDateTime.of(2025, 1, 1, 0, 15), 14.0, 0.8));
            assertFedIn(inverter, JANUARY, 4.0);

            inverter.add(new SinglePowerData(LocalDateTime.of(2025, 2, 1, 0, 15), 13.0, 2.8));
            assertFedIn(inverter, FEBRUARY, 13.0 - 2.8);

            inverter.add(new SinglePowerData(LocalDateTime.of(2025, 3, 1, 0, 15), 15.432, 0.72));
            assertFedIn(inverter, MARCH, 15.432 - 0.72);
        }

        private static void assertFedIn(Inverter inverter, YearMonth january, double expected) {
            assertThat(inverter.fedInToGrid().get(january)).isCloseTo(expected, Offset.offset(0.00000001));
        }

        private static void assertThatNothingWasFedId(Inverter inverter, YearMonth month) {
            assertThat(inverter.fedInToGrid()).containsExactly(Map.entry(month, 0.0));
        }
    }
}