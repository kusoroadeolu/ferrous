package io.github.kusoroadeolu.ferrous.throwing;

@FunctionalInterface
public interface ThrowingRunnable {
    void run() throws Exception;
}
