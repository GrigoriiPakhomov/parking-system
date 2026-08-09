package parking.model.parking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import parking.model.loyalty.LoyaltyAccount;
import parking.model.vehicle.PassengerCar;
import parking.model.vehicle.Truck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TruckParkingSpotTest {

    @Test
    @DisplayName("Грузовое место должно позволять разместить один грузовик")
    void testAllowOneTruck() {
        TruckParkingSpot spot = new TruckParkingSpot("T1");
        Truck truck = new Truck("T1", new LoyaltyAccount());

        spot.setCurrentVehicle(truck);

        assertThat(spot.hasTruck()).isTrue();
        assertThat(spot.getCurrentVehicle()).isSameAs(truck);
        assertThat(spot.getPassengerCount()).isZero();
    }

    @Test
    @DisplayName("Грузовое место должно позволять разместить два легковых автомобиля")
    void testAllowTwoPassengerCars() {
        TruckParkingSpot spot = new TruckParkingSpot("T1");
        PassengerCar car1 = new PassengerCar("P1", new LoyaltyAccount());
        PassengerCar car2 = new PassengerCar("P2", new LoyaltyAccount());

        spot.addPassenger(car1);
        spot.addPassenger(car2);

        assertThat(spot.getPassengerCount()).isEqualTo(2);
        assertThat(spot.hasTruck()).isFalse();
    }

    @Test
    @DisplayName("Грузовое место не должно позволять разместить третью легковую машину")
    void testNotAllowThirdPassengerCar() {
        TruckParkingSpot spot = new TruckParkingSpot("T1");
        PassengerCar car1 = new PassengerCar("P1", new LoyaltyAccount());
        PassengerCar car2 = new PassengerCar("P2", new LoyaltyAccount());
        PassengerCar car3 = new PassengerCar("P3", new LoyaltyAccount());

        spot.addPassenger(car1);
        spot.addPassenger(car2);

        assertThatThrownBy(() -> spot.addPassenger(car3))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Грузовое место не должно позволять разместить легковую машину при наличии грузовика")
    void testNotAllowPassengerCarWhenTruckIsParked() {
        TruckParkingSpot spot = new TruckParkingSpot("T1");
        Truck truck = new Truck("T1", new LoyaltyAccount());
        PassengerCar car = new PassengerCar("P1", new LoyaltyAccount());

        spot.setCurrentVehicle(truck);

        assertThatThrownBy(() -> spot.addPassenger(car))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Грузовое место не должно позволять разместить грузовик при наличии легковых машин")
    void testNotAllowTruckWhenPassengerCarsAreParked() {
        TruckParkingSpot spot = new TruckParkingSpot("T1");
        PassengerCar car1 = new PassengerCar("P1", new LoyaltyAccount());
        PassengerCar car2 = new PassengerCar("P2", new LoyaltyAccount());
        Truck truck = new Truck("T1", new LoyaltyAccount());

        spot.addPassenger(car1);
        spot.addPassenger(car2);

        assertThatThrownBy(() -> spot.setCurrentVehicle(truck))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Грузовое место должно стать свободным после удаления легковых машин")
    void testBecomeFreeAfterRemovingPassengerCars() {
        TruckParkingSpot spot = new TruckParkingSpot("T1");
        PassengerCar car1 = new PassengerCar("P1", new LoyaltyAccount());
        PassengerCar car2 = new PassengerCar("P2", new LoyaltyAccount());

        spot.addPassenger(car1);
        spot.addPassenger(car2);
        spot.removePassenger(car1);
        spot.removePassenger(car2);

        assertThat(spot.isFree()).isTrue();
        assertThat(spot.getPassengerCount()).isZero();
    }

    @Test
    @DisplayName("Грузовое место должно стать свободным после уезда грузовика")
    void testBecomeFreeAfterTruckLeaves() {
        TruckParkingSpot spot = new TruckParkingSpot("T1");
        Truck truck = new Truck("T1", new LoyaltyAccount());

        spot.setCurrentVehicle(truck);
        spot.setCurrentVehicle(null);

        assertThat(spot.isFree()).isTrue();
        assertThat(spot.hasTruck()).isFalse();
    }
}