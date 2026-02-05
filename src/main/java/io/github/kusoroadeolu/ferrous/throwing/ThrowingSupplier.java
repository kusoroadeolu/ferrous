package io.github.kusoroadeolu.ferrous.throwing;

@FunctionalInterface
public interface ThrowingSupplier<T> {
    T get() throws Exception;
}
