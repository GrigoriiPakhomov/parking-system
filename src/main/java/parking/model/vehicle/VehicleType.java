package parking.model.vehicle;

import lombok.Getter;

/**
 * Перечисляет типы автомобилей
 */

@Getter
public enum VehicleType {
    PASSENGER("Легковой"),
    TRUCK("Грузовой");

    private final String description;

    VehicleType(String description) {
        this.description = description;
    }
}


