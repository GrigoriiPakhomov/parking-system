package parking.model.parking;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import parking.model.vehicle.Vehicle;

/**
 * Базовый класс для всех типов парковочных мест.
 */
@Getter
@RequiredArgsConstructor
public abstract class ParkingSpot {

 private final String id;
 private final ParkingSpotType parkingSpotType;

 @Setter
 private Vehicle currentVehicle;

 /**
  * Проверяет, свободно ли парковочное место.
  *
  * @return true, если место свободно
  */
 public boolean isFree() {
  return currentVehicle == null;
 }
}