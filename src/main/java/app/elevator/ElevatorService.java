package app.elevator;

public class ElevatorService {
    private final ElevatorCabin[] cabins;
    private static ElevatorService instance;

    public ElevatorService(ElevatorCabin[] cabins) {
        this.cabins = cabins;
    }

    public static ElevatorService getInstance() {
        return instance;
    }

    public static void initialize(ElevatorService elevatorService) {
        if (instance == null) {
            instance = elevatorService;
        }
    }

    public synchronized ElevatorCabin requestCabin(int targetFloor) {
        ElevatorCabin cabin = findClosestPreferablyAvailable(targetFloor);
        cabin.addRequestedFloor(targetFloor);

        return cabin;
    }

    private ElevatorCabin findClosestPreferablyAvailable(int targetFloor) {
        int minDistance = Integer.MAX_VALUE;
        int distance;
        ElevatorCabin closest = null;
        for (ElevatorCabin cabin : cabins) {
            distance = Math.abs(cabin.getCurrentFloor() - targetFloor);

            if (closest == null) {
                minDistance = distance;
                closest = cabin;
            } else if (isClosestAvailable(minDistance, distance, closest, cabin)) {
                minDistance = distance;
                closest = cabin;
            }
        }

        return closest;
    }

    private boolean isClosestAvailable(int minDistance, int distance, ElevatorCabin closest, ElevatorCabin cabin) {
        return (distance < minDistance && cabin.isAvailable())
                || (distance < minDistance && !cabin.isAvailable() && !closest.isAvailable());
    }
}
