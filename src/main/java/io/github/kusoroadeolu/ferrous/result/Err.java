package io.github.kusoroadeolu.ferrous.result;

import io.github.kusoroadeolu.ferrous.option.None;
import io.github.kusoroadeolu.ferrous.option.Option;
import io.github.kusoroadeolu.ferrous.option.Pair;
import io.github.kusoroadeolu.ferrous.option.Some;
import io.github.kusoroadeolu.ferrous.utils.Utils;
import org.jspecify.annotations.NonNull;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public record Err<T, E>(@NonNull E error) implements Result<T, E> {

    public Err{
        Utils.throwIfNull(error);
    }

    @Override
    public boolean isOk() {
        return false;
    }

    @Override
    public boolean isErr() {
        return true;
    }

    @Override
    public @NonNull T unwrap() {
        throw new ResultException("unWrap() called on type 'err'");
    }

    @Override
    public @NonNull E unwrapErr() {
        return error;
    }

    @Override
    public @NonNull T unwrapOr(@NonNull T defaultValue) {
        return defaultValue;
    }

    @Override
    public @NonNull T unwrapOrElse(@NonNull Supplier<T> supplier) {
        return supplier.get();
    }

    @Override
    public @NonNull T expect(@NonNull String message) {
        throw new ResultException(message);
    }

    @Override
    public @NonNull E expectErr(@NonNull String message)  {
        return error;
    }

    @Override
    public @NonNull <U> Result<U, E> map(@NonNull Function<T, U> fn) {
        return new Err<>(error);
    }

    @Override
    public @NonNull <F> Result<T, F> mapErr(@NonNull Function<E, F> fn) {
        return new Err<>(fn.apply(error));
    }

    @Override
    public @NonNull <U> Result<U, E> flatMap(@NonNull Function<T, Result<U, E>> fn) {
        return new Err<>(error);
    }

    @Override
    public @NonNull <U> Result<U, E> and(@NonNull Result<U, E> other) {
        return other;
    }

    @Override
    public @NonNull <U> Result<U, E> andThen(@NonNull Function<T, Result<U, E>> fn) {
        return new Err<>(error);
    }

    @Override
    public @NonNull Result<T, E> or(@NonNull Result<T, E> other) {
        return other;
    }

    @Override
    public @NonNull Result<T, E> orElse(@NonNull Supplier<Result<T, E>> supplier) {
        return supplier.get();
    }

    @Override
    public @NonNull Option<T> ok() {
        return new None<>();
    }

    @Override
    public @NonNull Option<E> err() {
        return new Some<>(error);
    }

    @Override
    public boolean contains(@NonNull T value) {
        return false;
    }

    @Override
    public boolean containsErr(@NonNull E error) {
        return Objects.equals(this.error, error);
    }

    @Override
    public @NonNull Result<T, E> inspect(@NonNull Consumer<T> consumer) {
        return this;
    }

    @Override
    public @NonNull Result<T, E> inspectErr(@NonNull Consumer<E> consumer) {
        consumer.accept(error);
        return this;
    }

    @Override
    public @NonNull <U> Result<Pair<T, U>, E> zip(@NonNull Result<U, E> other) {
        return new Err<>(error);
    }

    @Override
    public @NonNull <U, R> Result<R, E> zipWith(@NonNull Result<U, E> other, @NonNull BiFunction<T, U, R> fn) {
        return new Err<>(error);
    }

    @Override
    public @NonNull <U> Option<Result<U, E>> transpose() {
        return new Some<>(new Err<>(error));
    }

    @Override
    public void ifOk(@NonNull Consumer<T> consumer) {

    }

    @Override
    public void ifErr(@NonNull Consumer<E> consumer) {
        consumer.accept(error);
    }

    @Override
    public void ifOkOrElse(@NonNull Consumer<T> onOk, @NonNull Consumer<E> onErr) {
        onErr.accept(error);
    }

    @Override
    public @NonNull Stream<T> stream() {
        return Stream.empty();
    }
}