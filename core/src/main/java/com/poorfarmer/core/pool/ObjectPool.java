package com.poorfarmer.core.pool;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ObjectPool<T> {

  private final Supplier<T> factory;
  private final Consumer<T> resetter;
  private final Object[] items;
  private int size;

  public ObjectPool(int capacity, Supplier<T> factory, Consumer<T> resetter) {
    if (capacity < 1) {
      throw new IllegalArgumentException("capacity must be positive");
    }
    this.factory = factory;
    this.resetter = resetter;
    this.items = new Object[capacity];
  }

  public T obtain() {
    if (size > 0) {
      @SuppressWarnings("unchecked")
      T item = (T) items[--size];
      if (resetter != null) {
        resetter.accept(item);
      }
      return item;
    }
    return factory.get();
  }

  public void recycle(T item) {
    if (item == null || size >= items.length) {
      return;
    }
    items[size++] = item;
  }

  public int pooledCount() {
    return size;
  }

  public int capacity() {
    return items.length;
  }
}
