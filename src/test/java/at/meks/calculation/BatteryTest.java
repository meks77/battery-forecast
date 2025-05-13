package at.meks.calculation;

import at.meks.powerdata.SinglePowerData;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class BatteryTest {

    public static final YearMonth JANUARY = YearMonth.of(2025, 1);
    private static final YearMonth FEBRUARY = YearMonth.of(2025, 2);
    private static final YearMonth MARCH = YearMonth.of(2025, 3);

    @Nested
    class FedIn {

        @Test
        void withinOneMonth() {
            var battery = new Battery(10.0, 6000);

            battery.add(new SinglePowerData(LocalDateTime.of(2025, 1, 1, 0, 15), 1.0, 0.8));
            assertThatNothingWasFedId(battery, JANUARY);

            battery.add(new SinglePowerData(LocalDateTime.of(2025, 1, 1, 0, 30), 3.0, 0.8));
            assertThatNothingWasFedId(battery, JANUARY);

            battery.add(new SinglePowerData(LocalDateTime.of(2025, 1, 1, 0, 45), 8.432, 0.8));
            assertFedIn(battery, JANUARY, 0.832);

            battery.add(new SinglePowerData(LocalDateTime.of(2025, 1, 1, 1, 0), 10.0, 0.8));
            assertFedIn(battery, JANUARY, 10.032);

            battery.add(new SinglePowerData(LocalDateTime.of(2025, 1, 2, 0, 0), 10.0, 0.8));
            assertFedIn(battery, JANUARY, 19.232);
        }

        @Test
        void differentMonths() {
            var battery = new Battery(10.0, 6000);

            battery.add(new SinglePowerData(LocalDateTime.of(2025, 1, 1, 0, 15), 14.0, 0.8));
            assertFedIn(battery, JANUARY, 4.0);

            battery.add(new SinglePowerData(LocalDateTime.of(2025, 2, 1, 0, 15), 13.0, 2.8));
            assertFedIn(battery, FEBRUARY, 13.0 - 2.8);

            battery.add(new SinglePowerData(LocalDateTime.of(2025, 3, 1, 0, 15), 15.432, 0.72));
            assertFedIn(battery, MARCH, 15.432 - 0.72);
        }

        private static void assertFedIn(Battery battery, YearMonth january, double expected) {
            assertThat(battery.fedInToGrid().get(january)).isCloseTo(expected, Offset.offset(0.00000001));
        }

        private static void assertThatNothingWasFedId(Battery battery, YearMonth january) {
            assertThat(battery.fedInToGrid()).containsExactly(Map.entry(january, 0.0));
        }
    }
}