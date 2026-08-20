package parking.model.parking;

import lombok.Getter;
import parking.model.vehicle.Vehicle;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Представляет парковку, содержащую парковочные места
 * и очередь автомобилей, ожидающих освобождения места.
 */
@Getter
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
     */
    public Parking(List<PassengerParkingSpot> passengerSpots, List<TruckParkingSpot> truckSpots, int maxQueueSize) {
        this.passengerSpots = passengerSpots;
        this.truckSpots = truckSpots;
        this.waitingQueue = new LinkedList<>();
        this.maxQueueSize = maxQueueSize;
    }
    public int getMaxQueueSize() {
        return maxQueueSize;
    }
}