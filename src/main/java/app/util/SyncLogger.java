package app.util;

public class SyncLogger {
    private static final Object LOCK = new Object();

    public static void log(String message) {
        synchronized (LOCK) {
            System.out.println(message);
        }
    }
}
