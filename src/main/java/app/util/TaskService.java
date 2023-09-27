package app.util;

public class TaskService {
    public static Thread startTask(Thread task, Runnable runnable) {
        if (task != null && task.isAlive())
            return task;

        task = new Thread(runnable);
        task.start();

        return task;
    }
}
