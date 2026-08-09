package parking;

import parking.model.loyalty.LoyaltyAccount;
import parking.model.parking.PassengerParkingSpot;
import parking.model.parking.Parking;
import parking.model.parking.TruckParkingSpot;
import parking.model.vehicle.PassengerCar;
import parking.model.vehicle.Truck;
import parking.model.vehicle.Vehicle;
import parking.service.ParkingService;
import parking.service.VehicleTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Запускает симуляцию работы парковки.
 */
public class ParkingSimulation {

    /**
     * Запускает симуляцию парковки.
     *
     * @throws InterruptedException если главный поток был прерван
     */
    public void run() throws InterruptedException {

        Parking parking = createParking();
        ParkingService parkingService = new ParkingService(parking);

        List<Vehicle> vehicles = createVehicles();

        List<Thread> threads = new ArrayList<>();

        for (Vehicle vehicle : vehicles) {
            Thread thread =
                    new Thread(new VehicleTask(vehicle, parkingService));

            thread.setName(vehicle.getId());
            thread.start();

            threads.add(thread);
        }

        waitForCompletion(threads);
    }

    private Parking createParking() {

        List<PassengerParkingSpot> passengerSpots = List.of(
                new PassengerParkingSpot("P1"),
                new PassengerParkingSpot("P2")
        );

        List<TruckParkingSpot> truckSpots = List.of(
                new TruckParkingSpot("T1")
        );

        return new Parking(passengerSpots, truckSpots);
    }

    private List<Vehicle> createVehicles() {

        return List.of(
                new Truck("Грузовик-1", new LoyaltyAccount()),
                new PassengerCar("Легковой-1", new LoyaltyAccount()),
                new PassengerCar("Легковой-2", new LoyaltyAccount()),
                new PassengerCar("Легковой-3", new LoyaltyAccount()),
                new PassengerCar("Легковой-4", new LoyaltyAccount()),
                new Truck("Грузовик-2", new LoyaltyAccount())
        );
    }

    private void waitForCompletion(List<Thread> threads)
            throws InterruptedException {

        for (Thread thread : threads) {
            thread.join();
        }
    }
}