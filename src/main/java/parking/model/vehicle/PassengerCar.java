package parking.model.vehicle;

import parking.model.loyalty.LoyaltyAccount;

import static parking.model.vehicle.VehicleType.PASSENGER;

/**
 * Легковой автомобиль в системе парковки.
 */
public class PassengerCar extends Vehicle {
    public PassengerCar(String id, LoyaltyAccount loyaltyAccount) {
        super(id, PASSENGER, loyaltyAccount);
    }
}