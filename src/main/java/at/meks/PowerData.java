package at.meks;

import java.time.LocalDateTime;

public record PowerData(LocalDateTime timestamp, double fedInKwh, double consumptionKwh) {

}
