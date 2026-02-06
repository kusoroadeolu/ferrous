package io.github.kusoroadeolu.ferrous.result;

import io.github.kusoroadeolu.ferrous.option.None;
import io.github.kusoroadeolu.ferrous.option.Option;
import io.github.kusoroadeolu.ferrous.option.Pair;
import io.github.kusoroadeolu.ferrous.option.Some;
import org.jspecify.annotations.NonNull;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public record Ok<T, E>(@NonNull T value) implements Result<T, E> {
    @Override
    public boolean isOk() {
        return true;
    }

    @Override
    public boolean isErr() {
        return false;
    }

    @Override
    public @NonNull T unwrap() {
        return value;
    }

    @Override
    public @NonNull E unwrapErr() {
        throw new ResultException("unwrapErr() called on type 'ok'");
    }

    @Override
    public @NonNull T unwrapOr(@NonNull T defaultValue) {
        return value;
    }

    @Override
    public @NonNull T unwrapOrElse(@NonNull Supplier<T> supplier) {
        return value;
    }

    @Override
    public @NonNull T expect(@NonNull String message) {
        return value;
    }

    @Override
    public @NonNull E expectErr(@NonNull String message) {
        throw new ResultException(message);
    }

    @Override
    public @NonNull <U> Result<U, E> map(@NonNull Function<T, U> fn) {
         return new Ok<>(fn.apply(value));
    }

    @Override
    public @NonNull <F> Result<T, F> mapErr(@NonNull Function<E, F> fn) {
        return new Ok<>(value);
    }

    @Override
    public @NonNull <U> Result<U, E> flatMap(@NonNull Function<T, Result<U, E>> fn) {
        return fn.apply(value);
    }

    @Override
    public @NonNull <U> Result<U, E> and(@NonNull Result<U, E> other) {
        return other;
    }

    @Override
    public @NonNull <U> Result<U, E> andThen(@NonNull Function<T, Result<U, E>> fn) {
        return this.flatMap(fn);
    }

    @Override
    public @NonNull Result<T, E> or(@NonNull Result<T, E> other) {
        return this;
    }

    @Override
    public @NonNull Result<T, E> orElse(@NonNull Supplier<Result<T, E>> supplier) {
        return this;
    }


    @Override
    public @NonNull Option<T> ok() {
        return new Some<>(value);
    }

    @Override
    public @NonNull Option<E> err() {
        return new None<>();
    }

    @Override
    public boolean contains(@NonNull T value) {
        return Objects.equals(this.value, value);
    }

    @Override
    public boolean containsErr(@NonNull E error) {
        return false;
    }

    @Override
    public @NonNull Result<T, E> inspect(@NonNull Consumer<T> consumer) {
        consumer.accept(value);
        return this;
    }

    @Override
    public @NonNull Result<T, E> inspectErr(@NonNull Consumer<E> consumer) {
        return this;
    }

    @Override
    public @NonNull <U> Result<Pair<T, U>, E> zip(@NonNull Result<U, E> other) {
        return switch (other){
            case Ok<U, E> ok -> new Ok<>(new Pair<>(value, ok.value));
            case Err<U, E> err -> new Err<>(err.error());
        };
    }

    @Override
    public @NonNull <U, R> Result<R, E> zipWith(@NonNull Result<U, E> other, @NonNull BiFunction<T, U, R> fn) {
        return switch (other){
            case Ok<U, E> ok -> new Ok<>(fn.apply(value, ok.value));
            case Err<U, E> err -> new Err<>(err.error());
        };
    }

    @Override
    public @NonNull <U> Option<Result<U, E>> transpose() {
        return (Option<Result<U,E>>) switch (this.value) {
            case Some<?> some -> new Some<>(new Ok<>(some.value()));
            default -> new None<>();
        };
    }

    @Override
    public void ifOk(@NonNull Consumer<T> consumer) {
        consumer.accept(value);
    }

    @Override
    public void ifErr(@NonNull Consumer<E> consumer) {}

    @Override
    public void ifOkOrElse(@NonNull Consumer<T> onOk, @NonNull Consumer<E> onErr) {
        onOk.accept(value);
    }

    @Override
    public @NonNull Stream<T> stream() {
        return Stream.of(value);
    }
}
