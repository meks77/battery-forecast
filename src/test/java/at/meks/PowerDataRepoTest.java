package at.meks;

import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PowerDataRepoTest {

    private final PowerDataRepo repo = new PowerDataRepo();

    @Nested
    class Years {

        @Test
        void onlyYearsContainingPowerDataAreReturned() {
            repo.add(new PowerData(LocalDateTime.of(2022, 12, 31, 23, 59), 1.0, 0.0));
            repo.add(new PowerData(LocalDateTime.of(2023, 1, 1, 0, 0), 1.0, 0.0));
            repo.add(new PowerData(LocalDateTime.of(2023, 1, 1, 0, 1), 1.0, 0.0));
            repo.add(new PowerData(LocalDateTime.of(2023, 1, 1, 0, 2), 1.0, 0.0));
            repo.add(new PowerData(LocalDateTime.of(2024, 1, 1, 0, 0), 1.0, 0.0));

            Collection<Integer> result = repo.years();
            assertThat(result).containsExactly(2022, 2023);
        }
    }

    @Nested
    class AsListOfYear {

        @Test
        void onlyDataContainingPowerDataWithinTheYearAreReturned() {
            repo.add(new PowerData(LocalDateTime.of(2022, 12, 31, 23, 59), 1.0, 0.0));
            repo.add(new PowerData(LocalDateTime.of(2023, 1, 1, 0, 0), 1.0, 0.0));
            repo.add(new PowerData(LocalDateTime.of(2023, 1, 1, 0, 1), 1.0, 0.0));
            repo.add(new PowerData(LocalDateTime.of(2023, 12, 31, 23, 59, 59), 1.0, 0.0));
            repo.add(new PowerData(LocalDateTime.of(2024, 1, 1, 0, 0, 0), 1.0, 0.0));
            repo.add(new PowerData(LocalDateTime.of(2024, 1, 1, 0, 1, 0), 1.0, 0.0));

            List<PowerData> result = repo.asList(2023);

            assertThat(result).containsExactly(
                    new PowerData(LocalDateTime.of(2023, 1, 1, 0, 1), 1.0, 0.0),
                    new PowerData(LocalDateTime.of(2023, 12, 31, 23, 59, 59), 1.0, 0.0),
                    new PowerData(LocalDateTime.of(2024, 1, 1, 0, 0), 1.0, 0.0)
            );
        }
    }


}