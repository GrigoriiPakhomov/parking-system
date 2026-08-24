package parking.model.parking;

import static parking.model.parking.ParkingSpotType.PASSENGER;

/**
 * Легковое место: только 1 легковой.
 */
public class PassengerParkingSpot extends ParkingSpot {
    public PassengerParkingSpot(String id) {
        super(id, PASSENGER);
    }
}