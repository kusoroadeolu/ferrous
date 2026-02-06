package io.github.kusoroadeolu.ferrous.option;

import io.github.kusoroadeolu.ferrous.result.Err;
import io.github.kusoroadeolu.ferrous.result.Ok;
import io.github.kusoroadeolu.ferrous.result.Result;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.*;
import java.util.stream.Stream;

public record None<T>() implements Option<T> {
    @Override
    public boolean isSome() {
        return false;
    }

    @Override
    public boolean isNone() {
        return true;
    }

    @Override
    public @NonNull T unwrap() {
        throw new OptionException("unwrap() called on 'none' type");
    }

    @Override
    public @NonNull T unwrapOr(@NonNull T fallback) {
        return fallback;
    }

    @Override
    public @NonNull T unwrapOrElse(@NonNull Supplier<T> supplier) {
        return supplier.get();
    }

    @Override
    public @NonNull T expect(@NonNull String message) {
        throw new OptionException(message);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull <U> Option<U> map(@NonNull Function<T, U> fn) {
        return (Option<U>) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull <U> Option<U> flatMap(@NonNull Function<T, Option<U>> fn) {
        return (Option<U>) this;
    }

    @Override
    public @NonNull Option<T> filter(@NonNull Predicate<T> predicate) {
        return this;
    }

    @Override
    public @NonNull <U> Option<U> and(@NonNull Option<U> other) {
        return other;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull <U> Option<U> andThen(@NonNull Function<T, Option<U>> fn) {
        return (Option<U>) this;
    }

    @Override
    public @NonNull Option<T> or(@NonNull Option<T> other) {
        return other;
    }

    @Override
    public @NonNull Option<T> orElse(@NonNull Supplier<Option<T>> supplier) {
        var val = supplier.get();
        return val == null ? new None<>() : val;
    }


    @Override
    public @Nullable T toNullable() {
        return null;
    }

    @Override
    public boolean contains(@NonNull T value) {
        return false;
    }

    @Override
    public @NonNull Option<T> inspect(@NonNull Consumer<T> consumer) {
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull <U> Option<Pair<T, U>> zip(@NonNull Option<U> other) {
        return (Option<Pair<T,U>>) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull <U, R> Option<R> zipWith(@NonNull Option<U> other, @NonNull BiFunction<T, U, R> fn) {
        return (Option<R>) this;
    }

    @Override
    public @NonNull <E> Result<T, E> okOr(@NonNull E error) {
        return new Err<>(error);
    }

    @Override
    public @NonNull <E> Result<T, E> okOrElse(@NonNull Supplier<E> supplier) {
        return okOr(supplier.get());
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull <U, E> Result<Option<U>, E> transpose() {
        return new Ok<>((Option<U>) this);
    }

    @Override
    public @NonNull Option<T> xor(@NonNull Option<T> other) {
        return other;
    }

    @Override
    public void ifSome(@NonNull Consumer<T> consumer) {}

    @Override
    public void ifNone(@NonNull Runnable runnable) {
        runnable.run();
    }

    @Override
    public @NonNull Stream<T> stream() {
        return Stream.empty();
    }

    @Override
    public @NonNull Optional<T> toOptional() {
        return Optional.empty();
    }
}
