package parking.model.parking;

import parking.model.vehicle.Vehicle;
import parking.model.vehicle.VehicleType;

import java.util.ArrayList;
import java.util.List;

import static parking.model.parking.ParkingSpotType.TRUCK;

/**
 * Грузовое парковочное место.
 * Может вместить либо один грузовой автомобиль,
 * либо до двух легковых автомобилей.
 */
public class TruckParkingSpot extends ParkingSpot {

    private final List<Vehicle> passengers = new ArrayList<>();

    public TruckParkingSpot(String id) {
        super(id, TRUCK);
    }

    /**
     * Размещает грузовой автомобиль на парковочном месте.
     *
     * @param vehicle грузовой автомобиль
     * @throws IllegalArgumentException если передан не грузовой автомобиль
     */
    @Override
    public void setCurrentVehicle(Vehicle vehicle) {
        if (vehicle != null && vehicle.getVehicleType() != VehicleType.TRUCK) {
            throw new IllegalArgumentException(
                    "Грузовое парковочное место предназначено только для грузового автомобиля."
            );
        }

        if (vehicle != null && !passengers.isEmpty()) {
            throw new IllegalStateException(
                    "Нельзя разместить грузовик: место занято легковыми автомобилями."
            );
        }

        super.setCurrentVehicle(vehicle);
    }

    /**
     * Добавляет легковой автомобиль на грузовое парковочное место.
     *
     * @param vehicle легковой автомобиль
     * @throws IllegalArgumentException если автомобиль не легковой
     * @throws IllegalStateException если место занято грузовиком
     * @throws IllegalStateException если на месте уже два легковых автомобиля
     */
    public void addPassenger(Vehicle vehicle) {
        if (vehicle.getVehicleType() != VehicleType.PASSENGER) {
            throw new IllegalArgumentException(
                    "На грузовое парковочное место можно добавить только легковой автомобиль."
            );
        }

        if (hasTruck()) {
            throw new IllegalStateException(
                    "Нельзя разместить автомобиль: место занято грузовиком."
            );
        }

        if (passengers.size() >= 2) {
            throw new IllegalStateException(
                    "Нельзя разместить автомобиль: грузовое парковочное место заполнено."
            );
        }

        passengers.add(vehicle);
    }

    /**
     * Удаляет легковой автомобиль с грузового парковочного места.
     *
     * @param vehicle автомобиль, который покидает место
     */
    public void removePassenger(Vehicle vehicle) {
        passengers.remove(vehicle);
    }

    /**
     * Возвращает количество легковых автомобилей на месте.
     *
     * @return количество легковых автомобилей
     */
    public int getPassengerCount() {
        return passengers.size();
    }

    /**
     * Проверяет, занято ли место грузовым автомобилем.
     *
     * @return true, если место занято грузовым автомобилем
     */
    public boolean hasTruck() {
        return getCurrentVehicle() != null;
    }

    /**
     * Проверяет, полностью ли свободно грузовое место.
     *
     * @return true, если на месте нет автомобилей
     */
    @Override
    public boolean isFree() {
        return getCurrentVehicle() == null && passengers.isEmpty();
    }


}