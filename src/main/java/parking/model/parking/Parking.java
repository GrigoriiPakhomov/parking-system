package parking.model.parking;

import parking.model.vehicle.Vehicle;
import parking.model.vehicle.VehicleType;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Представляет парковку, содержащую парковочные места
 * и очередь автомобилей, ожидающих освобождения места.
 */
public class Parking {
    private final List<PassengerParkingSpot> passengerSpots;
    private final List<TruckParkingSpot> truckSpots;
    private final Queue<Vehicle> waitingQueue;
    private final int maxQueueSize;

    /**
     * Создает парковку.
     *
     * @param passengerSpots легковые парковочные места
     * @param truckSpots грузовые парковочные места
     * @param maxQueueSize максимальный размер очереди ожидания
     */
    public Parking(
            List<PassengerParkingSpot> passengerSpots,
            List<TruckParkingSpot> truckSpots,
            int maxQueueSize
    ) {
        this.passengerSpots = List.copyOf(passengerSpots);
        this.truckSpots = List.copyOf(truckSpots);
        this.waitingQueue = new LinkedList<>();
        this.maxQueueSize = maxQueueSize;
    }

    /**
     * Находит свободное легковое парковочное место.
     *
     * @return свободное место или {@code null}, если свободного места нет
     */
    public PassengerParkingSpot findFreePassengerSpot() {
        for (PassengerParkingSpot spot : passengerSpots) {
            if (spot.isFree()) {
                return spot;
            }
        }

        return null;
    }

    /**
     * Находит грузовое парковочное место, на которое можно поставить грузовик.
     *
     * @return свободное грузовое место или {@code null}
     */
    public TruckParkingSpot findFreeTruckSpot() {
        for (TruckParkingSpot spot : truckSpots) {
            if (spot.isFree()) {
                return spot;
            }
        }

        return null;
    }

    /**
     * Находит грузовое место, на которое можно поставить легковой автомобиль.
     *
     * @return подходящее грузовое место или {@code null}
     */
    public TruckParkingSpot findTruckSpotForPassenger() {
        for (TruckParkingSpot spot : truckSpots) {
            if (spot.canAcceptPassenger()) {
                return spot;
            }
        }

        return null;
    }

    /**
     * Проверяет, есть ли свободное место для автомобиля указанного типа.
     *
     * @param vehicleType тип автомобиля
     * @return true, если подходящее место существует
     */
    public boolean hasAvailableSpot(VehicleType vehicleType) {
        return switch (vehicleType) {
            case PASSENGER -> findFreePassengerSpot() != null || findTruckSpotForPassenger() != null;
            case TRUCK -> findFreeTruckSpot() != null;
        };
    }

    /**
     * Добавляет автомобиль в очередь ожидания.
     *
     * @param vehicle автомобиль
     * @return true, если автомобиль добавлен в очередь
     */
    public boolean addToWaitingQueue(Vehicle vehicle) {
        if (waitingQueue.size() >= maxQueueSize) {
            return false;
        }

        return waitingQueue.offer(vehicle);
    }

    /**
     * Возвращает первый автомобиль из очереди без удаления.
     *
     * @return первый ожидающий автомобиль или {@code null}
     */
    public Vehicle peekWaitingVehicle() {
        return waitingQueue.peek();
    }

    /**
     * Удаляет первый автомобиль из очереди.
     *
     * @return удалённый автомобиль или {@code null}
     */
    public Vehicle pollWaitingVehicle() {
        return waitingQueue.poll();
    }

    /**
     * Проверяет, пуста ли очередь ожидания.
     *
     * @return true, если очередь пуста
     */
    public boolean isWaitingQueueEmpty() {
        return waitingQueue.isEmpty();
    }

    /**
     * Возвращает текущий размер очереди ожидания.
     *
     * @return размер очереди
     */
    public int getWaitingQueueSize() {
        return waitingQueue.size();
    }

    /**
     * Возвращает максимальный размер очереди.
     *
     * @return максимальный размер очереди
     */
    public int getMaxQueueSize() {
        return maxQueueSize;
    }
}