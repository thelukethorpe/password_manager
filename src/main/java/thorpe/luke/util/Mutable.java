package thorpe.luke.util;

import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Consumer;

public class Mutable<T> {
  private final Collection<Consumer<T>> updateListeners;
  private T value;

  public Mutable(T value) {
    this.updateListeners = new LinkedList<>();
    this.value = value;
  }

  public T get() {
    return value;
  }

  public T getOrDefault(T defaultValue) {
    return value != null ? value : defaultValue;
  }

  public void set(T value) {
    if (!this.value.equals(value)) {
      this.value = value;
      updateListeners.forEach(updateListener -> updateListener.accept(value));
    }
  }

  public void addUpdateListener(Consumer<T> updateListener) {
    updateListeners.add(updateListener);
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) {
      return true;
    } else if (object instanceof Mutable) {
      Mutable that = (Mutable) object;
      return this.value.equals(that.value);
    }
    return false;
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
