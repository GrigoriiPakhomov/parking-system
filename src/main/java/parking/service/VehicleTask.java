package parking.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parking.model.vehicle.Vehicle;
import parking.model.vehicle.VehicleType;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Задача, выполняемая отдельным потоком автомобиля.
 *
 * <p>Автомобиль приезжает на парковку, ожидает свободное место,
 * стоит случайное время и после этого покидает парковку.</p>
 */
@RequiredArgsConstructor
public class VehicleTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(VehicleTask.class);
    private final Vehicle vehicle;
    private final ParkingService parkingService;

    private static final long PASSEGER_MIN_STAY_MS = 1_000L;
    private static final long PASSEGER_MAX_STAY_MS = 3_000L;
    private static final long TRUCK_MIN_STAY_MS = 2_000L;
    private static final long TRUCK_MAX_STAY_MS = 5_000L;

    @Override
    public void run() {
        try {
            log.info("[{}] приехал.", vehicle.getId());

            parkingService.parkVehicle(vehicle);
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
            return ThreadLocalRandom.current().nextLong(PASSEGER_MIN_STAY_MS, PASSEGER_MAX_STAY_MS + 1);
        }
        return ThreadLocalRandom.current().nextLong(TRUCK_MIN_STAY_MS, TRUCK_MAX_STAY_MS + 1);
    }
}