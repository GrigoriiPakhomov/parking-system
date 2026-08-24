package parking.model.vehicle;

import parking.model.loyalty.LoyaltyAccount;

import static parking.model.vehicle.VehicleType.TRUCK;

/**
 * Грузовой автомобиль в системе парковки.
 */
public class Truck extends Vehicle {
    public Truck(String id, LoyaltyAccount loyaltyAccount) {
        super(id, TRUCK, loyaltyAccount);
    }
}
