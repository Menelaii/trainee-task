package app;

import app.elevator.ElevatorCabin;
import app.event.Subscription;
import app.util.SyncLogger;

public class Person {
    private final String name;
    private final int weight;
    private final int targetFloor;

    private Subscription<ElevatorCabin> onDoorsOpenWhileOutside;
    private Subscription<ElevatorCabin> onFloorChanged;
    private Subscription<ElevatorCabin> onDoorsOpenWhileInside;

    private int currentFloor;

    public Person(String name, int weight, int currentFloor, int targetFloor) {
        this.name = name;
        this.weight = weight;
        this.currentFloor = currentFloor;
        this.targetFloor = targetFloor;
    }

    public void useElevator(ElevatorCabin elevatorCabin) {
        SyncLogger.log("["+ name +"]" + " ждёт лифт на этаже: "  + currentFloor);
        if (elevatorCabin.getCurrentFloor() == currentFloor) {
            enter(elevatorCabin);
        } else {
            onDoorsOpenWhileOutside = elevatorCabin
                            .getOnDoorsOpen()
                            .addListener(this::onDoorsOpenWhileOutside);
        }
    }

    private void enter(ElevatorCabin elevatorCabin) {
        SyncLogger.log("["+ name +"]" + " зашёл на этаже " + currentFloor);

        elevatorCabin.onPassengerEnters(weight);
        elevatorCabin.addRequestedFloor(targetFloor);

        onFloorChanged = elevatorCabin
                .getOnFloorChanged()
                .addListener(this::onFloorChanged);

        onDoorsOpenWhileInside = elevatorCabin
                .getOnDoorsOpen()
                .addListener(this::onDoorsOpenWhileInside);
    }

    private void exit(ElevatorCabin elevatorCabin) {
        elevatorCabin.onPassengerLeaves(weight);
        onFloorChanged.unsubscribe();
        onDoorsOpenWhileInside.unsubscribe();

        SyncLogger.log("["+ name +"]" + " вышел на этаже " + targetFloor);
    }

    private void onDoorsOpenWhileOutside(ElevatorCabin attachedTo) {
        if (this.currentFloor == attachedTo.getCurrentFloor()) {
            enter(attachedTo);
            onDoorsOpenWhileOutside.unsubscribe();
        }
    }

    private void onDoorsOpenWhileInside(ElevatorCabin attachedTo) {
        if (this.targetFloor == attachedTo.getCurrentFloor()) {
            exit(attachedTo);
            onDoorsOpenWhileInside.unsubscribe();
        }
    }

    private void onFloorChanged(ElevatorCabin attachedTo) {
        this.currentFloor = attachedTo.getCurrentFloor();
    }
}
