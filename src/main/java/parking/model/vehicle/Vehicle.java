package parking.model.vehicle;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import parking.model.loyalty.LoyaltyAccount;
import parking.model.parking.ParkingSpot;

/**
 * Базовый класс для всех типов автомобилей в системе парковки.
 */
@Getter
@RequiredArgsConstructor
public abstract class Vehicle {

    private final String id;
    private final VehicleType vehicleType;
    private final LoyaltyAccount loyaltyAccount;

    @Setter
    private ParkingSpot parkingSpot;
}