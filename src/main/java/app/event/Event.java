package app.event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Event<T> {
    private final List<Consumer<T>> listeners;

    public Event() {
        this.listeners = new ArrayList<>();
    }

    public Subscription<T> addListener(Consumer<T> listener) {
        listeners.add(listener);
        return new Subscription<>(this, listener);
    }

    public void removeListener(Consumer<T> listener) {
        listeners.remove(listener);
    }


    /**
    * итерация по копиям идёт для поддержки отписки из обработчика события
    * */
    public void trigger(T input) {
        List<Consumer<T>> copyOfListeners = new ArrayList<>(listeners);
        copyOfListeners.forEach(listener -> listener.accept(input));
    }
}
