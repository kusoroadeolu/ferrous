package io.github.kusoroadeolu.ferrous.option;

import io.github.kusoroadeolu.ferrous.result.Err;
import io.github.kusoroadeolu.ferrous.result.Ok;
import io.github.kusoroadeolu.ferrous.result.Result;
import org.jspecify.annotations.NonNull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.*;
import java.util.stream.Stream;

public record Some<T>(T value) implements Option<T> {

    @Override
    public boolean isSome() {
        return true;
    }

    @Override
    public boolean isNone() {
        return false;
    }

    @Override
    public @NonNull T unwrap() {
        return value;
    }

    @Override
    public @NonNull T unwrapOr(@NonNull T fallback) {
        return value;
    }

    @Override
    public @NonNull T unwrapOrElse(@NonNull Supplier<@NonNull T> supplier) {
        return value;
    }

    @Override
    public @NonNull T expect(@NonNull String message) {
        return value;
    }

    @Override
    public @NonNull <U> Option<U> map(@NonNull Function<T, @NonNull U> fn) {
        return new Some<>(fn.apply(value));
    }

    @Override
    public @NonNull <U> Option<U> flatMap(@NonNull Function<T, @NonNull Option<U>> fn) {
        return fn.apply(value);
    }

    @Override
    public @NonNull Option<T> filter(@NonNull Predicate<T> predicate) {
        return predicate.test(value) ? this : new None<>();
    }

    @Override
    public @NonNull <U> Option<U> and(@NonNull Option<U> other) {
        return other;
    }

    @Override
    public @NonNull <U> Option<U> andThen(@NonNull Function<T, @NonNull Option<U>> fn) {
        return this.flatMap(fn);
    }

    @Override
    public @NonNull Option<T> or(@NonNull Option<T> other) {
        return this;
    }

    @Override
    public @NonNull Option<T> orElse(@NonNull Supplier<@NonNull Option<T>> supplier) {
        return this;
    }

    @Override
    public T toNullable() {
        return value;
    }

    @Override
    public boolean contains(@NonNull T value) {
        return Objects.equals(this.value, value);
    }

    @Override
    public @NonNull Option<T> inspect(@NonNull Consumer<T> consumer) {
        consumer.accept(value);
        return this;
    }

    @Override
    public @NonNull <U> Option<@NonNull Pair<T, U>> zip(@NonNull Option<U> other) {
        return switch (other){
            case Some<U> some -> {
                var pair = new Pair<>(value, some.value);
                yield new Some<>(pair);
            }
            case None<U> _ -> new None<>();
        };
    }

    @Override
    public @NonNull <U, R> Option<R> zipWith(@NonNull Option<U> other, @NonNull BiFunction<T, @NonNull U, @NonNull R> fn) {
        return switch (other){
            case Some<U> some -> new Some<>(fn.apply(value, some.value));
            case None<U> _ -> new None<>();
        };
    }

    @Override
    public @NonNull <E> Result<T, E> okOr(@NonNull E error) {
        return new Ok<>(value);
    }

    @Override
    public @NonNull <E> Result<T, E> okOrElse(@NonNull Supplier<@NonNull E> supplier) {
        return new Ok<>(value);
    }

    @Override
    @SuppressWarnings("unchecked")
    @NonNull
    public <U, E> Result<Option<U>, E> transpose() {
        var result = (Result<U, E>) this.value;
        return switch (result) {
            case Ok<U, E> ok -> new Ok<>(new Some<>(ok.value()));
            case Err<U, E> err -> new Err<>(err.error());
        };
    }

    @Override
    public @NonNull Option<T> xor(@NonNull Option<T> other) {
        return this;
    }

    @Override
    public void ifSome(@NonNull Consumer<T> consumer) {
        consumer.accept(value);
    }

    @Override
    public void ifNone(@NonNull Runnable runnable) {
        //Does nothing
    }


    @Override
    public @NonNull Stream<T> stream() {
        return Stream.of(value);
    }

    @Override
    public @NonNull Optional<T> toOptional() {
        return Optional.of(value);
    }
}
