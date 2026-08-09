package parking.model.parking;

import lombok.Getter;

/**
 * Перечисляет типы парковковочных мест
 */

@Getter
public enum ParkingSpotType {
    PASSENGER("Парковочное место для легкового"),
    TRUCK("Парковочное место для грузового");

    private final String description;

    ParkingSpotType(String description) {
        this.description = description;
    }
}
