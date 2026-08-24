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
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

class ParkingServiceTest {

    @Test
    @DisplayName("Легковой автомобиль должен парковаться на легковом месте")
    void testParkPassengerCarOnPassengerSpot() {
        PassengerParkingSpot passengerSpot = new PassengerParkingSpot("P1");
        Parking parking = new Parking(List.of(passengerSpot), List.of(), 10);
        ParkingService service = new ParkingService(parking);
        PassengerCar car = new PassengerCar("P1", new LoyaltyAccount());

        CompletableFuture<Void> signal = service.parkVehicle(car);

        assertThat(signal).isCompleted();
        assertThat(passengerSpot.getCurrentVehicle()).isSameAs(car);
        assertThat(car.getParkingSpot()).isSameAs(passengerSpot);
    }

    @Test
    @DisplayName("Грузовик должен парковаться на грузовом месте")
    void testParkTruckOnTruckSpot() {
        TruckParkingSpot truckSpot = new TruckParkingSpot("T1");
        Parking parking = new Parking(List.of(), List.of(truckSpot), 10);
        ParkingService service = new ParkingService(parking);
        Truck truck = new Truck("T1", new LoyaltyAccount());

        CompletableFuture<Void> signal = service.parkVehicle(truck);

        assertThat(signal).isCompleted();
        assertThat(truckSpot.getCurrentVehicle()).isSameAs(truck);
        assertThat(truck.getParkingSpot()).isSameAs(truckSpot);
    }

    @Test
    @DisplayName("На грузовом месте должны размещаться два легковых автомобиля")
    void testAllowTwoPassengerCarsOnTruckSpot() {
        TruckParkingSpot truckSpot = new TruckParkingSpot("T1");
        Parking parking = new Parking(List.of(), List.of(truckSpot), 10);
        ParkingService service = new ParkingService(parking);
        PassengerCar car1 = new PassengerCar("P1", new LoyaltyAccount());
        PassengerCar car2 = new PassengerCar("P2", new LoyaltyAccount());
        CompletableFuture<Void> signal1 = service.parkVehicle(car1);
        CompletableFuture<Void> signal2 = service.parkVehicle(car2);

        assertThat(signal1).isCompleted();
        assertThat(signal2).isCompleted();

        assertThat(truckSpot.getPassengerCount()).isEqualTo(2);
        assertThat(car1.getParkingSpot()).isSameAs(truckSpot);
        assertThat(car2.getParkingSpot()).isSameAs(truckSpot);
    }

    @Test
    @DisplayName("Автомобиль должен получить сигнал после освобождения места")
    void testVehicleReceivesSignalAfterSpotIsFreed() {
        PassengerParkingSpot spot = new PassengerParkingSpot("P1");
        Parking parking = new Parking(List.of(spot), List.of(), 10);
        ParkingService service = new ParkingService(parking);
        PassengerCar first = new PassengerCar("P1", new LoyaltyAccount());
        PassengerCar second = new PassengerCar("P2", new LoyaltyAccount());

        service.parkVehicle(first);

        CompletableFuture<Void> secondSignal = service.parkVehicle(second);

        assertThat(secondSignal).isNotCompleted();
        assertThat(second.getParkingSpot()).isNull();

        service.leaveParking(first);

        assertThat(secondSignal).isCompleted();
        assertThat(second.getParkingSpot()).isSameAs(spot);
        assertThat(spot.getCurrentVehicle()).isSameAs(second);
    }

    @Test
    @DisplayName("При уезде автомобиля его скидка должна увеличиваться на один процент")
    void testIncreaseDiscountWhenVehicleLeaves() {
        PassengerParkingSpot spot = new PassengerParkingSpot("P1");
        Parking parking = new Parking(List.of(spot), List.of(), 10);
        ParkingService service = new ParkingService(parking);
        PassengerCar car = new PassengerCar("P1", new LoyaltyAccount());

        service.parkVehicle(car);
        service.leaveParking(car);

        assertThat(car.getLoyaltyAccount().getCurrentDiscount()).isEqualTo(1);
        assertThat(car.getParkingSpot()).isNull();
        assertThat(spot.getCurrentVehicle()).isNull();
    }
}