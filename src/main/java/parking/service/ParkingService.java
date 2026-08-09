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

/**
 * Управляет парковкой автомобилей и обеспечивает потокобезопасный
 * доступ к парковочным местам и очереди ожидания.
 */
@RequiredArgsConstructor
public class ParkingService {

    private static final Logger log =
            LoggerFactory.getLogger(ParkingService.class);

    private final Parking parking;

    /**
     * Паркует автомобиль.
     *
     * <p>Если свободного подходящего места нет, автомобиль
     * добавляется в FIFO-очередь и ожидает освобождения места.</p>
     *
     * @param vehicle автомобиль
     * @throws InterruptedException если поток был прерван во время ожидания
     */
    public void parkVehicle(Vehicle vehicle) throws InterruptedException {
        synchronized (parking) {

            if (!parking.getWaitingQueue().isEmpty()) {
                parking.getWaitingQueue().add(vehicle);

                log.info("[{}] встал в очередь. Позиция: {}",
                        vehicle.getId(),
                        parking.getWaitingQueue().size());

                waitForParking(vehicle);
                return;
            }

            if (tryPark(vehicle)) {
                logParking(vehicle);
                return;
            }

            parking.getWaitingQueue().add(vehicle);

            log.info("[{}] встал в очередь. Позиция: {}",
                    vehicle.getId(),
                    parking.getWaitingQueue().size());

            waitForParking(vehicle);
        }
    }

    /**
     * Ожидает, пока автомобиль станет первым в очереди
     * и для него появится подходящее место.
     */
    private void waitForParking(Vehicle vehicle)
            throws InterruptedException {

        while (true) {
            parking.wait();

            if (parking.getWaitingQueue().peek()==vehicle
                    && tryPark(vehicle)) {

                parking.getWaitingQueue().poll();

                logParking(vehicle);

                return;
            }
        }
    }

    /**
     * Пытается найти подходящее свободное место.
     *
     * @param vehicle автомобиль
     * @return true, если автомобиль припаркован
     */
    private boolean tryPark(Vehicle vehicle) {
        if (vehicle.getVehicleType()==VehicleType.PASSENGER) {
            return parkPassengerCar(vehicle);
        }

        return parkTruck(vehicle);
    }

    /**
     * Пытается припарковать легковой автомобиль.
     *
     * @param vehicle легковой автомобиль
     * @return true, если автомобиль припаркован
     */
    private boolean parkPassengerCar(Vehicle vehicle) {

        for (PassengerParkingSpot spot :
                parking.getPassengerSpots()) {

            if (spot.isFree()) {
                spot.setCurrentVehicle(vehicle);
                vehicle.setParkingSpot(spot);
                return true;
            }
        }

        for (TruckParkingSpot spot :
                parking.getTruckSpots()) {

            if (spot.getPassengerCount() < 2
                    && !spot.hasTruck()) {

                spot.addPassenger(vehicle);
                vehicle.setParkingSpot(spot);
                return true;
            }
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

        for (TruckParkingSpot spot :
                parking.getTruckSpots()) {

            if (spot.isFree()) {
                spot.setCurrentVehicle(vehicle);
                vehicle.setParkingSpot(spot);
                return true;
            }
        }

        return false;
    }

    /**
     * Освобождает место после отъезда автомобиля
     * и уведомляет ожидающие потоки.
     *
     * @param vehicle автомобиль, покидающий парковку
     */
    public void leaveParking(Vehicle vehicle) {
        synchronized (parking) {

            ParkingSpot spot = vehicle.getParkingSpot();

            if (spot==null) {
                log.warn("[{}] пытается покинуть парковку, "
                                + "но автомобиль не припаркован.",
                        vehicle.getId());
                return;
            }

            if (spot instanceof PassengerParkingSpot passengerSpot) {
                passengerSpot.setCurrentVehicle(null);

            } else if (spot instanceof TruckParkingSpot truckSpot) {

                if (truckSpot.getCurrentVehicle()==vehicle) {
                    truckSpot.setCurrentVehicle(null);
                } else {
                    truckSpot.removePassenger(vehicle);
                }
            }

            vehicle.setParkingSpot(null);

            vehicle.getLoyaltyAccount().upDiscount();

            log.info("[{}] уехал. Текущая скидка: {}%",
                    vehicle.getId(),
                    vehicle.getLoyaltyAccount().getCurrentDiscount());

            parking.notifyAll();
        }
    }

    /**
     * Записывает в лог информацию о занятом парковочном месте.
     *
     * @param vehicle припаркованный автомобиль
     */
    private void logParking(Vehicle vehicle) {
        ParkingSpot spot = vehicle.getParkingSpot();

        if (spot instanceof PassengerParkingSpot) {
            log.info("[{}] занял легковое место {}.",
                    vehicle.getId(),
                    spot.getId());

        } else if (spot instanceof TruckParkingSpot truckSpot) {

            if (vehicle.getVehicleType()==VehicleType.TRUCK) {
                log.info("[{}] занял грузовое место {}.",
                        vehicle.getId(),
                        spot.getId());
            } else {
                log.info("[{}] занял грузовое место ({}/2).",
                        vehicle.getId(),
                        truckSpot.getPassengerCount());
            }
        }
    }
}