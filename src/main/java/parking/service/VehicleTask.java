package parking.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parking.model.vehicle.Vehicle;
import parking.model.vehicle.VehicleType;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Задача, выполняемая отдельным потоком автомобиля.
 *
 * <p>Автомобиль приезжает на парковку, ожидает сигнал о свободном месте,
 * стоит случайное время и после этого покидает парковку.</p>
 */
@RequiredArgsConstructor
public class VehicleTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(VehicleTask.class);

    private final Vehicle vehicle;
    private final ParkingService parkingService;
    private static final long PASSENGER_MIN_STAY_MS = 1_000L;
    private static final long PASSENGER_MAX_STAY_MS = 3_000L;
    private static final long TRUCK_MIN_STAY_MS = 2_000L;
    private static final long TRUCK_MAX_STAY_MS = 5_000L;

    @Override
    public void run() {
        try {
            log.info("[{}] приехал.", vehicle.getId());
            CompletableFuture<Void> parkingSignal = parkingService.parkVehicle(vehicle);
            parkingSignal.join();
            Thread.sleep(generateStayTime());
            parkingService.leaveParking(vehicle);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
            log.warn("[{}] поток автомобиля был прерван.", vehicle.getId(), e);
        }
    }

    /**
     * Генерирует случайное время стоянки автомобиля.
     *
     * @return время стоянки в миллисекундах
     */
    private long generateStayTime() {
        if (vehicle.getVehicleType()==VehicleType.PASSENGER) {
            return ThreadLocalRandom.current().nextLong(PASSENGER_MIN_STAY_MS, PASSENGER_MAX_STAY_MS + 1);
        }

        return ThreadLocalRandom.current().nextLong(TRUCK_MIN_STAY_MS, TRUCK_MAX_STAY_MS + 1);
    }
}