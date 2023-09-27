package app.elevator;

import app.event.Event;
import app.util.SyncLogger;
import app.util.TaskService;

import java.util.ArrayList;
import java.util.List;

public class ElevatorCabin {
    private static final int DEFAULT_FLOOR = 1;
    private static final CabinStatus DEFAULT_CABIN_STATUS = CabinStatus.Rests;
    private static final int DOORS_DELAY = 1000;
    private static final int SECONDS_PER_FLOOR = 1000;
    private static final int WAIT_FOR_PASSENGERS_DELAY = 2000;

    private final int id;
    private final int maxWeight;
    private final Event<ElevatorCabin> onFloorChanged;
    private final Event<ElevatorCabin> onDoorsOpen;
    private final List<Integer> requestedFloors;

    private int currentWeight;
    private int currentFloor;
    private int targetFloor;
    private CabinStatus status;
    private Thread openDoorsTask;
    private Thread closeDoorsTask;
    private Thread movingTask;
    private Thread onDestinationReachedTask;

    public ElevatorCabin(int id, int maxWeight) {
        this.id = id;
        this.maxWeight = maxWeight;
        this.onFloorChanged = new Event<>();
        this.onDoorsOpen = new Event<>();
        this.requestedFloors = new ArrayList<>();

        currentFloor = DEFAULT_FLOOR;
        status = DEFAULT_CABIN_STATUS;
    }

    public Event<ElevatorCabin> getOnFloorChanged() {
        return onFloorChanged;
    }

    public Event<ElevatorCabin> getOnDoorsOpen() {
        return onDoorsOpen;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public int getId() {
        return id;
    }

    public boolean isAvailable() {
        return currentWeight < maxWeight;
    }

    public synchronized void addRequestedFloor(int floor) {
        requestedFloors.add(floor);

        targetFloor = recalculateTarget();

        if (status == CabinStatus.Rests) {
            if (currentFloor == targetFloor) {
                requestedFloors.remove(Integer.valueOf(targetFloor));
                return;
            }

            startMovingTask();
        }
    }

    public void onPassengerEnters(int passengerWeight) {
        if (status == CabinStatus.InMove) {
            throw new IllegalStateException();
        }

        currentWeight += passengerWeight;
    }

    public void onPassengerLeaves(int passengerWeight) {
        if (status != CabinStatus.Rests && status != CabinStatus.Temporary_Stop) {
            throw new IllegalStateException();
        }

        currentWeight -= passengerWeight;
    }

    private Thread startMovingTask() {
        return TaskService.startTask(movingTask, this::moveTask);
    }

    private Thread startOpenDoorsTask() {
        return TaskService.startTask(openDoorsTask, this::openDoorsTask);
    }

    private Thread startCloseDoorsTask() {
        return TaskService.startTask(closeDoorsTask, this::closeDoorsTask);
    }

    private Thread startOnDestinationReachedTask() {
        return TaskService.startTask(onDestinationReachedTask, this::onDestinationReached);
    }

    private void closeDoorsTask() {
        try {
            CabinStatus previousStatus = status;
            status = CabinStatus.Opening_Doors;
            SyncLogger.log("[Кабинка "+ id + "] " + "Двери закрываются");
            Thread.sleep(DOORS_DELAY);
            SyncLogger.log("[Кабинка "+ id + "] " + "Двери закрыты");
            status = previousStatus;

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void openDoorsTask() {
        try {
            CabinStatus previousStatus = status;
            status = CabinStatus.Closing_Doors;
            SyncLogger.log("[Кабинка "+ id + "] " + "Двери открываются");
            Thread.sleep(DOORS_DELAY);
            SyncLogger.log("[Кабинка "+ id + "] " + "Двери открыты");
            status = previousStatus;
            onDoorsOpen.trigger(this);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void moveTask() {
        try {
            startCloseDoorsTask().join();

            SyncLogger.log("[Кабинка "+ id + "] " + "начала движение с этажа " + currentFloor);

            status = CabinStatus.InMove;
            while(currentFloor != targetFloor) {
                Thread.sleep(SECONDS_PER_FLOOR);
                currentFloor = targetFloor - currentFloor > 0
                        ? currentFloor + 1 : currentFloor - 1;
                onFloorChanged.trigger(this);
            }

            startOnDestinationReachedTask();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void onDestinationReached() {
        try {
            status = CabinStatus.Temporary_Stop;

            startOpenDoorsTask().join();

            requestedFloors.remove(Integer.valueOf(targetFloor));


            targetFloor = recalculateTarget();

            if (!requestedFloors.isEmpty()) {
                SyncLogger.log("[Кабинка "+ id + "] " + "ожидание пока люди войдут или выйдут");
                Thread.sleep(WAIT_FOR_PASSENGERS_DELAY);

                startMovingTask();
            } else {
                SyncLogger.log("[Кабинка "+ id + "] " + "закончила движение");
                status = CabinStatus.Rests;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private int recalculateTarget() {
        int distance = 0;
        int minDistance = Integer.MAX_VALUE;
        int target = currentFloor;
        for (int requestedFloor : requestedFloors) {
            distance = Math.abs(requestedFloor - currentFloor);
            if (distance < minDistance) {
                minDistance = distance;
                target = requestedFloor;
            }
        }

        return target;
    }
}
