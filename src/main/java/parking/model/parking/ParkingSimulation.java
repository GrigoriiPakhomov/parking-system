package parking.model.parking;

import parking.model.loyalty.LoyaltyAccount;
import parking.model.vehicle.PassengerCar;
import parking.model.vehicle.Truck;
import parking.model.vehicle.Vehicle;
import parking.service.ParkingService;
import parking.service.VehicleTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Запускает симуляцию работы парковки.
 */
public class ParkingSimulation {

    private static final int MAX_QUEUE_SIZE = 10;
    private static final int TOTAL_VEHICLES = 20;

    /**
     * Запускает симуляцию парковки.
     *
     * @throws InterruptedException если главный поток был прерван
     */
    public void run() throws InterruptedException {
        Parking parking = createParking();
        ParkingService parkingService = new ParkingService(parking);
        List<Vehicle> vehicles = createVehicles();

        List<Thread> threads = createAndStartThreads(vehicles, parkingService);

        waitForCompletion(threads);
    }

    /**
     * Создаёт парковку с легковыми и грузовыми парковочными местами.
     *
     * @return созданная парковка
     */
    private Parking createParking() {
        List<PassengerParkingSpot> passengerSpots = List.of(
                new PassengerParkingSpot("P1"),
                new PassengerParkingSpot("P2")
        );

        List<TruckParkingSpot> truckSpots = List.of(new TruckParkingSpot("T1"));

        return new Parking(passengerSpots, truckSpots, MAX_QUEUE_SIZE);
    }

    /**
     * Создаёт автомобили для симуляции.
     *
     * <p>Автомобили создаются циклом:
     * нечётные номера получают легковые автомобили,
     * чётные номера — грузовики.</p>
     *
     * @return список автомобилей
     */
    private List<Vehicle> createVehicles() {
        List<Vehicle> vehicles = new ArrayList<>();

        for (int i = 1; i <= TOTAL_VEHICLES; i++) {
            LoyaltyAccount loyaltyAccount = new LoyaltyAccount();

            if (ThreadLocalRandom.current().nextBoolean()) {
                vehicles.add(new Truck("Грузовик-" + i, loyaltyAccount));
            } else {
                vehicles.add(new PassengerCar("Легковой-" + i, loyaltyAccount));
            }
        }

        return vehicles;
    }

    /**
     * Создаёт и запускает поток для каждого автомобиля.
     *
     * @param vehicles       список автомобилей
     * @param parkingService сервис парковки
     * @return список запущенных потоков
     */
    private List<Thread> createAndStartThreads(List<Vehicle> vehicles, ParkingService parkingService) {
        List<Thread> threads = new ArrayList<>();

        for (Vehicle vehicle : vehicles) {
            Thread thread = new Thread(new VehicleTask(vehicle, parkingService));
            thread.setName(vehicle.getId());
            thread.start();
            threads.add(thread);
        }

        return threads;
    }

    /**
     * Ожидает завершения всех потоков симуляции.
     *
     * @param threads потоки автомобилей
     * @throws InterruptedException если главный поток был прерван
     */
    private void waitForCompletion(List<Thread> threads)
            throws InterruptedException {

        for (Thread thread : threads) {
            thread.join();
        }
    }
}