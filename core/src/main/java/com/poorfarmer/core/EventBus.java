package com.poorfarmer.core;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class EventBus {

  private final Map<Class<?>, List<Consumer<?>>> listeners = new ConcurrentHashMap<>();

  public <T> void subscribe(Class<T> type, Consumer<T> listener) {
    listeners.computeIfAbsent(type, key -> new CopyOnWriteArrayList<>()).add(listener);
  }

  public <T> void unsubscribe(Class<T> type, Consumer<T> listener) {
    List<Consumer<?>> list = listeners.get(type);
    if (list != null) {
      list.remove(listener);
    }
  }

  public void clear() {
    listeners.clear();
  }

  @SuppressWarnings("unchecked")
  public <T> void publish(T event) {
    List<Consumer<?>> list = listeners.get(event.getClass());
    if (list == null) {
      return;
    }
    for (Consumer<?> consumer : list) {
      ((Consumer<T>) consumer).accept(event);
    }
  }
}
