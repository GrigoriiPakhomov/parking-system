package parking.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parking.model.parking.Parking;
import parking.model.parking.ParkingSpot;
import parking.model.parking.PassengerParkingSpot;
import parking.model.parking.TruckParkingSpot;
import parking.model.vehicle.Vehicle;
import parking.model.vehicle.VehicleType;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Управляет парковкой автомобилей и обеспечивает потокобезопасный
 * доступ к парковочным местам и очереди ожидания.
 */
@RequiredArgsConstructor
public class ParkingService {
    private static final Logger log = LoggerFactory.getLogger(ParkingService.class);
    private final Parking parking;
    private final Map<Vehicle, CompletableFuture<Void>> waitingSignals = new HashMap<>();

    /**
     * Запрашивает парковочное место для автомобиля.
     *
     * <p>Если подходящее место уже существует, автомобиль паркуется
     * немедленно, а возвращаемый Future уже завершён.</p>
     *
     * <p>Если места нет, автомобиль добавляется в FIFO-очередь,
     * а Future будет завершён после освобождения подходящего места.</p>
     *
     * @param vehicle автомобиль
     * @return сигнал, который сообщает автомобилю о возможности продолжить
     * @throws IllegalStateException если очередь ожидания заполнена
     */
    public CompletableFuture<Void> parkVehicle(Vehicle vehicle) {
        synchronized (parking) {
            if (parking.isWaitingQueueEmpty() && tryPark(vehicle)) {
                logParking(vehicle);
                return CompletableFuture.completedFuture(null);
            }

            CompletableFuture<Void> parkingSignal = new CompletableFuture<>();

            if (!parking.addToWaitingQueue(vehicle)) {
                throw new IllegalStateException("Очередь ожидания парковки переполнена.");
            }

            waitingSignals.put(vehicle, parkingSignal);

            log.info(
                    "[{}] встал в очередь. Позиция: {}",
                    vehicle.getId(),
                    parking.getWaitingQueueSize()
            );

            return parkingSignal;
        }
    }

    /**
     * Пытается припарковать автомобиль.
     *
     * @param vehicle автомобиль
     * @return true, если автомобиль припаркован
     */
    private boolean tryPark(Vehicle vehicle) {
        return switch (vehicle.getVehicleType()) {
            case PASSENGER -> parkPassengerCar(vehicle);
            case TRUCK -> parkTruck(vehicle);
        };
    }

    /**
     * Пытается припарковать легковой автомобиль.
     *
     * @param vehicle легковой автомобиль
     * @return true, если автомобиль припаркован
     */
    private boolean parkPassengerCar(Vehicle vehicle) {
        PassengerParkingSpot passengerSpot = parking.findFreePassengerSpot();

        if (passengerSpot != null) {
            passengerSpot.setCurrentVehicle(vehicle);
            vehicle.setParkingSpot(passengerSpot);
            return true;
        }

        TruckParkingSpot truckSpot = parking.findTruckSpotForPassenger();

        if (truckSpot != null) {
            truckSpot.addPassenger(vehicle);
            vehicle.setParkingSpot(truckSpot);
            return true;
        }

        return false;
    }

    /**
     * Пытается припарковать грузовой автомобиль.
     *
     * @param vehicle грузовой автомобиль
     * @return true, если автомобиль припаркован
     */
    private boolean parkTruck(Vehicle vehicle) {
        TruckParkingSpot truckSpot = parking.findFreeTruckSpot();

        if (truckSpot == null) {
            return false;
        }

        truckSpot.setCurrentVehicle(vehicle);
        vehicle.setParkingSpot(truckSpot);

        return true;
    }

    /**
     * Освобождает место после отъезда автомобиля
     * и передаёт сигнал следующему ожидающему автомобилю.
     *
     * @param vehicle автомобиль, покидающий парковку
     */
    public void leaveParking(Vehicle vehicle) {
        synchronized (parking) {
            ParkingSpot spot = vehicle.getParkingSpot();

            if (spot == null) {
                throw new IllegalStateException("Автомобиль не припаркован: " + vehicle.getId());
            }

            switch (spot) {
                case PassengerParkingSpot passengerSpot -> passengerSpot.setCurrentVehicle(null);
                case TruckParkingSpot truckSpot -> {
                    if (truckSpot.getCurrentVehicle() == vehicle) {
                        truckSpot.setCurrentVehicle(null);
                    } else {
                        truckSpot.removePassenger(vehicle);
                    }
                }

                default -> throw new IllegalStateException("Неизвестный тип парковочного места: " + spot.getClass().getName());
            }

            vehicle.setParkingSpot(null);
            vehicle.getLoyaltyAccount().upDiscount();

            log.info(
                    "[{}] уехал. Текущая скидка: {}%",
                    vehicle.getId(),
                    vehicle.getLoyaltyAccount().getCurrentDiscount()
            );

            signalNextVehicle();
        }
    }

    /**
     * Проверяет очередь и пытается припарковать первый ожидающий автомобиль.
     *
     * <p>Если для первого автомобиля пока нет подходящего места,
     * FIFO-очередь сохраняется.</p>
     */
    private void signalNextVehicle() {
        if (parking.isWaitingQueueEmpty()) {
            return;
        }

        Vehicle nextVehicle = parking.peekWaitingVehicle();

        if (!tryPark(nextVehicle)) {
            return;
        }

        parking.pollWaitingVehicle();

        CompletableFuture<Void> signal = waitingSignals.remove(nextVehicle);

        if (signal != null) {
            signal.complete(null);
        }

        logParking(nextVehicle);
    }

    /**
     * Записывает в лог информацию о занятом парковочном месте.
     *
     * @param vehicle припаркованный автомобиль
     */
    private void logParking(Vehicle vehicle) {
        ParkingSpot spot = vehicle.getParkingSpot();

        switch (spot) {
            case PassengerParkingSpot passengerSpot ->
                    log.info(
                            "[{}] занял легковое место {}.",
                            vehicle.getId(),
                            passengerSpot.getId()
                    );

            case TruckParkingSpot truckSpot -> {
                if (vehicle.getVehicleType() == VehicleType.TRUCK) {
                    log.info(
                            "[{}] занял грузовое место {}.",
                            vehicle.getId(),
                            truckSpot.getId()
                    );
                } else {
                    log.info(
                            "[{}] занял грузовое место ({}/2).",
                            vehicle.getId(),
                            truckSpot.getPassengerCount()
                    );
                }
            }

            default -> throw new IllegalStateException("Неизвестный тип парковочного места: " + spot.getClass().getName());
        }
    }
}