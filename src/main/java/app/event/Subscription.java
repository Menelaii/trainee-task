package app.event;

import java.util.function.Consumer;

public class Subscription<T> {
    private final Event<T> event;
    private final Consumer<T> listener;

    public Subscription(Event<T> event, Consumer<T> listener) {
        this.event = event;
        this.listener = listener;
    }

    public void unsubscribe() {
        event.removeListener(listener);
    }
}
