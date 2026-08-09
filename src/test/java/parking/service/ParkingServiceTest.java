package parking.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import parking.model.loyalty.LoyaltyAccount;
import parking.model.parking.Parking;
import parking.model.parking.PassengerParkingSpot;
import parking.model.parking.TruckParkingSpot;
import parking.model.vehicle.PassengerCar;
import parking.model.vehicle.Truck;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ParkingServiceTest {

    @Test
    @DisplayName("Легковой автомобиль должен парковаться на легковом месте")
    void testParkPassengerCarOnPassengerSpot() throws InterruptedException {
        PassengerParkingSpot passengerSpot = new PassengerParkingSpot("P1");
        Parking parking = new Parking(List.of(passengerSpot), List.of());
        ParkingService service = new ParkingService(parking);
        PassengerCar car = new PassengerCar("P1", new LoyaltyAccount());

        service.parkVehicle(car);

        assertThat(passengerSpot.getCurrentVehicle()).isSameAs(car);
        assertThat(car.getParkingSpot()).isSameAs(passengerSpot);
    }

    @Test
    @DisplayName("Грузовик должен парковаться на грузовом месте")
    void testParkTruckOnTruckSpot() throws InterruptedException {
        TruckParkingSpot truckSpot = new TruckParkingSpot("T1");
        Parking parking = new Parking(List.of(), List.of(truckSpot));
        ParkingService service = new ParkingService(parking);
        Truck truck = new Truck("T1", new LoyaltyAccount());

        service.parkVehicle(truck);

        assertThat(truckSpot.getCurrentVehicle()).isSameAs(truck);
        assertThat(truck.getParkingSpot()).isSameAs(truckSpot);
    }

    @Test
    @DisplayName("На грузовом месте должны размещаться два легковых автомобиля")
    void testAllowTwoPassengerCarsOnTruckSpot() throws InterruptedException {
        TruckParkingSpot truckSpot = new TruckParkingSpot("T1");
        Parking parking = new Parking(List.of(), List.of(truckSpot));
        ParkingService service = new ParkingService(parking);
        PassengerCar car1 = new PassengerCar("P1", new LoyaltyAccount());
        PassengerCar car2 = new PassengerCar("P2", new LoyaltyAccount());

        service.parkVehicle(car1);
        service.parkVehicle(car2);

        assertThat(truckSpot.getPassengerCount()).isEqualTo(2);
        assertThat(car1.getParkingSpot()).isSameAs(truckSpot);
        assertThat(car2.getParkingSpot()).isSameAs(truckSpot);
    }

    @Test
    @DisplayName("При уезде автомобиля его скидка должна увеличиваться на один процент")
    void testIncreaseDiscountWhenVehicleLeaves() throws InterruptedException {
        PassengerParkingSpot passengerSpot = new PassengerParkingSpot("P1");
        Parking parking = new Parking(List.of(passengerSpot), List.of());
        ParkingService service = new ParkingService(parking);
        PassengerCar car = new PassengerCar("P1", new LoyaltyAccount());

        service.parkVehicle(car);
        service.leaveParking(car);

        assertThat(car.getLoyaltyAccount().getCurrentDiscount()).isEqualTo(1);
        assertThat(car.getParkingSpot()).isNull();
        assertThat(passengerSpot.getCurrentVehicle()).isNull();
    }

    @Test
    @DisplayName("Грузовик не должен парковаться на легковом месте")
    void testNotAllowTruckOnPassengerSpot() throws InterruptedException {
        PassengerParkingSpot passengerSpot = new PassengerParkingSpot("P1");
        Parking parking = new Parking(List.of(passengerSpot), List.of());
        ParkingService service = new ParkingService(parking);
        Truck truck = new Truck("T1", new LoyaltyAccount());

        Thread thread = new Thread(() -> {
            try {
                service.parkVehicle(truck);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        thread.start();
        Thread.sleep(100);

        assertThat(passengerSpot.getCurrentVehicle()).isNull();

        thread.interrupt();
        thread.join();
    }
}