package io.github.kusoroadeolu.ferrous.throwing;

import org.jspecify.annotations.NonNull;

@FunctionalInterface
public interface ThrowingFunction<T, R> {
    @NonNull R apply(@NonNull T t) throws Exception;
}
