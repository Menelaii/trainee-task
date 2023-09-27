package app;

import app.elevator.ElevatorCabin;
import app.elevator.ElevatorService;
import app.util.SyncLogger;

public class App {
    public static void main(String[] args) {
        ElevatorCabin[] cabins = new ElevatorCabin[2];
        cabins[0] =  new ElevatorCabin(0, 400);
        cabins[1] =  new ElevatorCabin(1, 800);
        for (ElevatorCabin cabin : cabins) {
            cabin.getOnFloorChanged().addListener(
                    (c) -> SyncLogger.log(
                            "[Кабинка "+ c.getId() + "] " + "текущий этаж = " + c.getCurrentFloor())
            );
        }

        ElevatorService.initialize(new ElevatorService(cabins));

        Floor[] floors = new Floor[20];
        for (int i = 0; i < 20; i++) {
            floors[i] = new Floor(i + 1);
        }

        Person person1 = new Person("Биба", 80, 1, 14);
        Person person2 = new Person("Боба", 80, 4, 1);

        Thread show = new Thread(() -> {
            person1.useElevator(floors[0].requestCabin());

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            person2.useElevator(floors[3].requestCabin());
        });


        show.start();
    }
}
