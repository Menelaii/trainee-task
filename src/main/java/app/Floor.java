package app;

import app.elevator.ElevatorCabin;
import app.elevator.ElevatorService;

public class Floor {
    private final int number;

    public Floor(int number) {
        this.number = number;
    }

    public ElevatorCabin requestCabin() {
        return ElevatorService
                .getInstance()
                .requestCabin(number);
    }
}
