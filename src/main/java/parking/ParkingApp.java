package parking;

import parking.model.parking.ParkingSimulation;

/**
 * Точка входа в приложение.
 */
public class ParkingApp {

    public static void main(String[] args) throws InterruptedException {
        new ParkingSimulation().run();
    }
}